package communication;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallbackI implements VotingSystem.Callback {
    private VotingSystem.VotingServicePrx votingService;
    private ExecutorService executorService;

    public CallbackI(VotingSystem.VotingServicePrx votingService) {
        this.votingService = votingService;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void reportResponse(String response, com.zeroc.Ice.Current current) {
        System.out.println(response);
    }

    public void processBlock(String[] voterIds, com.zeroc.Ice.Current current) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("client_log.csv", true))) {
            writer.write("voterId,votingStation,isPrime,time\n");
            for (String voterId : voterIds) {
                executorService.submit(() -> {
                    long startTime = System.currentTimeMillis();

                    votingService.getVotingStation(voterId, VotingSystem.CallbackPrx.checkedCast(current.con.createProxy(current.id)));

                    long endTime = System.currentTimeMillis();
                    synchronized (writer) {
                        try {
                            writer.write(String.format("%s,%s\n", voterId, "Response received from server"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyStatus(String message, com.zeroc.Ice.Current current) {
        System.out.println("Status: " + message);
    }
}