package businessLogic;

import VotingSystem.CallbackPrx;
import persistence.DatabaseAccess;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class VotingManager {
    private PrimeManager primeManager = new PrimeManager();
    private DatabaseAccess databaseAccess = new DatabaseAccess();

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
        executor.submit(() -> {
            List<String> voterIds = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter("server_log.csv", true))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    voterIds.add(line.trim());
                }

                int blockSize = voterIds.size() / observers.size();
                int remainder = voterIds.size() % observers.size();

                int startIndex = 0;
                for (CallbackPrx callbackPrx : observers) {
                    int endIndex = startIndex + blockSize + (remainder > 0 ? 1 : 0);
                    remainder--;

                    List<String> block = voterIds.subList(startIndex, endIndex);
                    CallbackPrx observer = callbackPrx;

                    executor.submit(() -> {
                        try {
                            observer.processBlock(block.toArray(new String[0])); // Notificar al cliente
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    startIndex = endIndex;
                }

                writer.write("Total queries," + voterIds.size() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
