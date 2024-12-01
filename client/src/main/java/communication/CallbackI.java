package communication;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallbackI implements VotingSystem.Callback {
    private final VotingSystem.VotingServicePrx votingService;
    private final ExecutorService executorService;

    public CallbackI(VotingSystem.VotingServicePrx votingService) {
        this.votingService = votingService;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void reportResponse(String response, com.zeroc.Ice.Current current) {
        System.out.println(response);
    }


    public void processBlock(String[] voterIds, com.zeroc.Ice.Current current) {
        CountDownLatch latch = new CountDownLatch(voterIds.length);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("client_log.csv", true))) {
            writer.write("voterId,response,time\n");
            for (String voterId : voterIds) {
                executorService.submit(() -> {
                    long startTime = System.currentTimeMillis();
                    System.out.println("Requesting voting station for voter " + voterId);
                    String serverAnswer = votingService.getVotingStationSync(voterId);

                    long endTime = System.currentTimeMillis();
                    long timeTaken = endTime - startTime;
                    synchronized (writer) {
                        try {
                            writer.write(String.format("%s,%s,%d\n", voterId, serverAnswer, timeTaken));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    latch.countDown();
                });
            }
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyStatus(String message, com.zeroc.Ice.Current current) {
        System.out.println("Status: " + message);
    }
}