package businessLogic;

import VotingSystem.CallbackPrx;
import persistence.DatabaseAccess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class VotingManager {
    private final PrimeManager primeManager = new PrimeManager();
    private final DatabaseAccess databaseAccess = new DatabaseAccess();

    public String getVotingStation(String voterId) {
        try {
            String votingStation = databaseAccess.getVotingStation(voterId);
            int primeFactorsCount = primeManager.countPrimeFactors(Integer.parseInt(voterId));
            boolean isPrime = primeManager.isPrime(primeFactorsCount);
            if (isPrime) {
                return votingStation + ",1";
                // return "Voter " + voterId + " votes at: " + votingStation + " and its number of prime factors is prime.";
            } else {
                return votingStation + ",0";
                // return "Voter " + voterId + " votes at: " + votingStation + " and its number of prime factors is not prime.";
            }
        } catch (Exception e) {
            return "Error retrieving voting station: " + e.getMessage() + "\n";
        }
    }

    public String listVotingStations(String city) {
        try {
            List<String> stations = databaseAccess.getVotingStations(city);
            StringBuilder response = new StringBuilder("Voting stations in " + city + ":\n");
            for (String station : stations) {
                response.append(station).append("\n");
            }
            return response.toString();
        } catch (Exception e) {
            return "Error retrieving voting stations: " + e.getMessage() + "\n";
        }
    }

    public void uploadVoterFile(String filePath, List<CallbackPrx> observers, ExecutorService executor, CallbackPrx callback) {
        final int BLOCK_SIZE = 1000; // Tamaño del bloque
        executor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
                 BufferedWriter logWriter = new BufferedWriter(new FileWriter("server_log.csv", true))) {
    
                List<String> block = new ArrayList<>();
                Queue<CallbackPrx> availableObservers = new LinkedList<>(observers);
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                int blockCounter = 0;
                if(observers.isEmpty()) {
                    availableObservers.add(callback);
                    callback.reportResponse("No observers available, you will process the entire list.");
                }
    
                logWriter.write("blockNumber,observer,timeTaken\n");
                int numBlocks = 0;
    
                String voterId;
                long startTimeTotal = System.currentTimeMillis();
                while ((voterId = reader.readLine()) != null) {
                    block.add(voterId.trim());
                    
                    CallbackPrx tmpObserver = null;
                    if (block.size() == BLOCK_SIZE) {
                        numBlocks++;

                        boolean valid = false;
                        while(!valid) {
                            while (availableObservers.isEmpty()) {
                                Thread.sleep(10); // Espera activa hasta que haya un observer disponible
                            }
                            while(tmpObserver == null && !availableObservers.isEmpty()) {
                                tmpObserver = availableObservers.poll();
                                if(tmpObserver != null) {
                                    try {
                                        tmpObserver.ice_ping();
                                    } catch (Exception e) {
                                        tmpObserver = null;
                                    }
                                    valid = true;
                                }
                            }
                        }
                        final CallbackPrx observer = tmpObserver;
    
                        String[] voterBlock = block.toArray(new String[0]);
                        int currentBlockNumber = blockCounter++;
    
                        long startTime = System.currentTimeMillis();
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            try {
                                observer.processBlock(voterBlock);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, executor).thenAccept(v -> {
                            long endTime = System.currentTimeMillis();
                            long timeTaken = endTime - startTime;
    
                            synchronized (logWriter) {
                                try {
                                    logWriter.write(String.format("%d,%s,%d\n", currentBlockNumber,
                                            observer.ice_getIdentity().name, timeTaken));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
    
                            availableObservers.add(observer); // Liberar el observer
                        });
    
                        futures.add(future);
                        block.clear(); // Limpiar el bloque para el siguiente grupo
                    }
                }
    
                // Procesar bloque restante, si existe
                if (!block.isEmpty()) {
                    while (availableObservers.isEmpty()) {
                        Thread.sleep(10);
                    }
    
                    CallbackPrx observer = null;
                    if(numBlocks == 0) {
                        observer = callback;
                        callback.reportResponse("The list is smaller than the block size, you will process the entire list.");
                    } else {
                        observer = availableObservers.poll();
                    }
                    String[] voterBlock = block.toArray(new String[0]);
                    int currentBlockNumber = blockCounter++;
    
                    long startTime = System.currentTimeMillis();
                    final CallbackPrx finalObserver = observer;
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            finalObserver.processBlock(voterBlock);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, executor).thenAccept(v -> {
                        long endTime = System.currentTimeMillis();
                        long timeTaken = endTime - startTime;
    
                        synchronized (logWriter) {
                            try {
                                logWriter.write(String.format("%d,%s,%d\n", currentBlockNumber,
                                        finalObserver.ice_getIdentity(), timeTaken));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
    
                        availableObservers.add(finalObserver);
                    });
    
                    futures.add(future);
                }
    
                // Esperar a que todas las tareas terminen
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                long endTimeTotal = System.currentTimeMillis();
                long time = endTimeTotal - startTimeTotal;
                logWriter.write("Time taken: " + time);
                callback.reportResponse("Time taken: " + time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }    
}
