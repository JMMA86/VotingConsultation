package communication;

import VotingSystem.CallbackPrx;
import businessLogic.ClientManager;
import businessLogic.VotingManager;
import com.zeroc.Ice.Current;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VotingServiceI implements VotingSystem.VotingService {
    private final ClientManager clientManager = new ClientManager();
    private final VotingManager votingManager = new VotingManager();
    private final List<CallbackPrx> observers = new ArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public VotingServiceI() {
        Thread connectionChecker = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    checkConnections();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        connectionChecker.start();
    }

    @Override
    public void registerObserver(CallbackPrx observer, Current current) {
        observers.add(observer);
        clientManager.registerObserver(observer);
    }

    @Override
    public void unregisterObserver(CallbackPrx observer, Current current) {
        observers.remove(observer);
        clientManager.unregisterObserver(observer);
    }

    @Override
    public boolean registerVoter(String voterId, CallbackPrx callback, Current current) {
        return false;
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
            try {
                callback.reportResponse("Uploading file...");
                votingManager.uploadVoterFile(filePath, observers, executor);
                callback.reportResponse("File uploaded successfully.");
                callback.reportResponse("Metrics saved in server_log.csv");
            } catch (Exception e) {
                callback.reportResponse("Error uploading file: " + e.getMessage());
            }
        });
    }

    public void reportResponse(String message, CallbackPrx callback) {
        try {
            callback.reportResponse(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkConnections() {
        clientManager.checkConnections();
    }
}
