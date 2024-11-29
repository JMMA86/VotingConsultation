import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.zeroc.Ice.Current;

import VotingSystem.CallbackPrx;

public class VotingServiceI implements VotingSystem.VotingService {
    // Map to store voters and their respective callback proxies
    Map<String, CallbackPrx> voters = new HashMap<>();

    // Semaphore to control access
    Semaphore semaphore = new Semaphore(1);

    // ThreadPool to handle requests
    ExecutorService executor = Executors.newFixedThreadPool(5);

    // DAO for database interactions
    private final DatabaseAccess databaseAccess = new DatabaseAccess();

    // Observers list
    private List<CallbackPrx> observers = new ArrayList<>();

    public VotingServiceI() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    checkConnections();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void registerObserver(CallbackPrx observer, Current current) {
        observers.add(observer);
    }

    @Override
    public void unregisterObserver(CallbackPrx observer, Current current) {
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        List<CallbackPrx> failedObservers = new ArrayList<>();
        for (CallbackPrx observer : observers) {
            try {
                observer.reportResponse(message);
            } catch (Exception e) {
                failedObservers.add(observer);
                System.err.println("Failed to notify observer: " + e.getMessage());
            }
        }
        observers.removeAll(failedObservers);
    }    

    @Override
    public boolean registerVoter(String voterId, CallbackPrx callback, Current current) {
        try {
            semaphore.acquire();
            if (voters.containsKey(voterId)) {
                return false; // Voter already registered
            }
            voters.put(voterId, callback);
            System.out.println("Voter " + voterId + " registered.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void unRegisterVoter(String voterId, Current current) {
        try {
            semaphore.acquire();
            voters.remove(voterId);
            System.out.println("Voter " + voterId + " unregistered.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void getVotingStation(String voterId, CallbackPrx callback, Current current) {
        executor.submit(() -> {
            try {
                String votingStation = databaseAccess.getVotingStation(voterId);
                int primeFactorsCount = countPrimeFactors(Integer.parseInt(voterId));
                boolean isPrime = isPrime(primeFactorsCount);
                if (isPrime) {
                    notifyObservers("\nVoter " + voterId + " votes at: " + votingStation + " and its number of prime factors is prime.");
                } else {
                    notifyObservers("\nVoter " + voterId + " votes at: " + votingStation + " and its number of prime factors is not prime.");
                }
            } catch (Exception e) {
                notifyObservers("\nError retrieving voting station: " + e.getMessage());
            }
        });
    }

    @Override
    public void listVotingStations(String city, CallbackPrx callback, Current current) {
        executor.submit(() -> {
            try {
                List<String> stations = databaseAccess.getVotingStations(city);
                StringBuilder response = new StringBuilder("\nVoting stations in " + city + ":\n");
                for (String station : stations) {
                    response.append(station).append("\n");
                }
                notifyObservers(response.toString());
            } catch (Exception e) {
                notifyObservers("\nError retrieving voting stations: " + e.getMessage());
            }
        });
    }

    @Override
    public void uploadVoterFile(String filePath, CallbackPrx callback, Current current) {
        executor.submit(() -> {
            int totalQueries = 0;
            long totalStartTime = System.currentTimeMillis();
            notifyObservers("Proccesing...");
            
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
                BufferedWriter writer = new BufferedWriter(new FileWriter("server_log.csv", true))) {
                
                String line;
                writer.write("voterId,votingStation,isPrime,time\n");
                while ((line = reader.readLine()) != null) {
                    totalQueries++;
                    long startTime = System.currentTimeMillis();
                    String voterId = line.trim();
                    String votingStation = databaseAccess.getVotingStation(voterId);
                    
                    int primeFactorCount = countPrimeFactors(Integer.parseInt(voterId));
                    boolean isPrime = isPrime(primeFactorCount);
                    
                    long endTime = System.currentTimeMillis();
                    
                    writer.write(String.format(
                        "%s,%s,%d,%d\n",
                        voterId, votingStation, isPrime ? 1 : 0, endTime - startTime
                    ));
                }
                
                long totalEndTime = System.currentTimeMillis();
                String answer = "\nTotal queries: " + totalQueries + "\nTime elapsed: " + (totalEndTime - totalStartTime) + " ms.\n";
                notifyObservers(answer);
            } catch (IOException e) {
                notifyObservers("Error reading file: " + e.getMessage());
            } catch (Exception e) {
                notifyObservers("Error processing voter IDs: " + e.getMessage());
            }
        });
    }

    public int countPrimeFactors(int n) {
        int count = 0;
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                count++;
                n /= i;
            }
        }
        return count;
    }
    
    public boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private void checkConnections() {
        for (String voterId : voters.keySet()) {
            CallbackPrx callback = voters.get(voterId);
            try {
                callback.ice_ping();
            } catch (Exception e) {
                voters.remove(voterId);
                System.out.println("Voter " + voterId + " disconnected.");
            }
        }
    }
}