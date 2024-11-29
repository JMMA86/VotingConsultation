import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
                callback.reportResponse("Voter " + voterId + " votes at: " + votingStation);
            } catch (Exception e) {
                callback.reportResponse("Error retrieving voting station: " + e.getMessage());
            }
        });
    }

    @Override
    public void listVotingStations(String city, CallbackPrx callback, Current current) {
        executor.submit(() -> {
            try {
                List<String> stations = databaseAccess.getVotingStations(city);
                StringBuilder response = new StringBuilder("Voting stations in " + city + ":\n");
                for (String station : stations) {
                    response.append(station).append("\n");
                }
                callback.reportResponse(response.toString());
            } catch (Exception e) {
                callback.reportResponse("Error retrieving voting stations: " + e.getMessage());
            }
        });
    }

    @Override
    public void uploadVoterFile(String filePath, CallbackPrx callback, Current current) {
        executor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String voterId = line.trim();
                    String votingStation = databaseAccess.getVotingStation(voterId);
                    callback.reportResponse("Voter " + voterId + " votes at: " + votingStation);
                }
            } catch (IOException e) {
                callback.reportResponse("Error reading file: " + e.getMessage());
            } catch (Exception e) {
                callback.reportResponse("Error processing voter IDs: " + e.getMessage());
            }
        });
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