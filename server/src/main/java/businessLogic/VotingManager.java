package businessLogic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import persistence.DatabaseAccess;

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

    public String uploadVoterFile(String filePath) {
        int totalQueries = 0;
        long totalStartTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter("server_log.csv", true))) {
            
            String line;
            writer.write("voterId,votingStation,isPrime,time\n");
            while ((line = reader.readLine()) != null) {
                totalQueries++;
                long startTime = System.currentTimeMillis();
                String voterId = line.trim();
                String votingStation = databaseAccess.getVotingStation(voterId);
                
                int primeFactorCount = primeManager.countPrimeFactors(Integer.parseInt(voterId));
                boolean isPrime = primeManager.isPrime(primeFactorCount);
                
                long endTime = System.currentTimeMillis();
                
                writer.write(String.format(
                    "%s,%s,%d,%d\n",
                    voterId, votingStation, isPrime ? 1 : 0, endTime - startTime
                ));
            }
            
            long totalEndTime = System.currentTimeMillis();
            return "Total queries: " + totalQueries + "\nTime elapsed: " + (totalEndTime - totalStartTime) + " ms.\n";
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage() + "\n";
        } catch (Exception e) {
            return "Error processing voter IDs: " + e.getMessage() + "\n";
        }
    }
}
