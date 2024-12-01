package communication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zeroc.Ice.Current;

import VotingSystem.CallbackPrx;

import businessLogic.ClientManager;
import businessLogic.VotingManager;

public class VotingServiceI implements VotingSystem.VotingService {
    // Client Manager
    private ClientManager clientManager = new ClientManager();

    // Voting Manager
    private VotingManager votingManager = new VotingManager();

    // ThreadPool to handle requests
    ExecutorService executor = Executors.newFixedThreadPool(5);

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
        clientManager.registerObserver(observer);
    }

    @Override
    public void unregisterObserver(CallbackPrx observer, Current current) {
        clientManager.unregisterObserver(observer);
    }

    @Override
    public boolean registerVoter(String voterId, CallbackPrx callback, Current current) {
        return clientManager.registerVoter(voterId, callback);
    }

    @Override
    public void unRegisterVoter(String voterId, Current current) {
        clientManager.unRegisterVoter(voterId);
    }

    @Override
    public void getVotingStation(String voterId, CallbackPrx callback, Current current) {
        executor.submit(() -> {
            String answer = votingManager.getVotingStation(voterId);
            reportResponse(answer, callback);
        });
    }

    @Override
    public void listVotingStations(String city, CallbackPrx callback, Current current) {
        executor.submit(() -> {
            String answer = votingManager.listVotingStations(city);
            reportResponse(answer, callback);
        });
    }

    @Override
    public void uploadVoterFile(String filePath, CallbackPrx callback, Current current) {
        executor.submit(() -> {
            reportResponse("Proccesing...", callback);
            String answer = votingManager.uploadVoterFile(filePath);
            reportResponse(answer, callback);
        });
    }

    public void reportResponse(String message, CallbackPrx callback) {
        System.out.println(message);
        clientManager.reportResponse(message, callback);
    }

    private void checkConnections() {
        clientManager.checkConnections();
    }
}