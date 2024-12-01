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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class VotingManager {
    private final PrimeManager primeManager = new PrimeManager();
    private final DatabaseAccess databaseAccess = new DatabaseAccess();

    public String getVotingStation(String voterId) {
        try {
            String votingStation = databaseAccess.getVotingStation(voterId);
            int primeFactorsCount = primeManager.countPrimeFactors(Integer.parseInt(voterId));
            boolean isPrime = primeManager.isPrime(primeFactorsCount);
            if (isPrime) {
                return "Voter " + voterId + " votes at: " + votingStation + " and its number of prime factors is prime.\n";
            } else {
                return "Voter " + voterId + " votes at: " + votingStation + " and its number of prime factors is not prime.\n";
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

    public void uploadVoterFile(String filePath, List<CallbackPrx> observers, ExecutorService executor) {
        // Tamaño del bloque para procesamiento
        final int BLOCK_SIZE = 100; // Ajusta según el rendimiento esperado y memoria disponible

        executor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
                BufferedWriter logWriter = new BufferedWriter(new FileWriter("server_log.csv", true))) {

                List<String> block = new ArrayList<>();
                Queue<CallbackPrx> availableObservers = new LinkedList<>(observers);
                AtomicInteger processingBlocks = new AtomicInteger(0);
                CountDownLatch latch = new CountDownLatch(observers.size());

                logWriter.write("blockNumber,observer,timeTaken\n");
                String voterId;
                int blockCounter = 0;

                while ((voterId = reader.readLine()) != null) {
                    block.add(voterId.trim());

                    // Si el bloque está completo, procesarlo
                    if (block.size() == BLOCK_SIZE) {
                        while (availableObservers.isEmpty()) {
                            // Esperar a que un observer esté disponible
                            Thread.sleep(50);
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
                        }, executor);

                        future.thenAccept(v -> {
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
                            processingBlocks.decrementAndGet();
                            latch.countDown();
                        });

                        block.clear(); // Limpiar el bloque para el siguiente grupo de cédulas
                        processingBlocks.incrementAndGet();
                    }
                }

                // Asegurarse de procesar el bloque restante si no está vacío
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
                    }, executor);

                    future.thenAccept(v -> {
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
                        processingBlocks.decrementAndGet();
                        latch.countDown();
                    });

                    block.clear();
                    processingBlocks.incrementAndGet();
                }

                // Esperar a que todos los bloques terminen
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
