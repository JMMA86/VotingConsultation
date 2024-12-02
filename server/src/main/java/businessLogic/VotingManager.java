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
                return "Voter " + voterId + " votes at: " + votingStation + " and its number of prime factors is prime.";
            } else {
                return "Voter " + voterId + " votes at: " + votingStation + " and its number of prime factors is not prime.";
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
    
                logWriter.write("blockNumber,observer,timeTaken\n");
    
                String voterId;
                long startTimeTotal = System.currentTimeMillis();
                while ((voterId = reader.readLine()) != null) {
                    block.add(voterId.trim());
    
                    if (block.size() == BLOCK_SIZE) {
                        while (availableObservers.isEmpty()) {
                            Thread.sleep(10); // Espera activa hasta que haya un observer disponible
                        }
    
                        CallbackPrx observer = availableObservers.poll();
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
                        Thread.sleep(500);
                    }
    
                    CallbackPrx observer = availableObservers.poll();
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
    
                        availableObservers.add(observer);
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
