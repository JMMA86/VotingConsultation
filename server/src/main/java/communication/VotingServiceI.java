package communication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;

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
    ExecutorService executor = Executors.newFixedThreadPool(4);

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
        clientManager.registerObserver(observer);
    }

    @Override
    public void unregisterObserver(CallbackPrx observer, Current current) {
        observers.remove(observer);
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
            reportResponse("Processing...", callback);
            List<String> lines = votingManager.uploadVoterFile(filePath);  // Leer el archivo

            int observerCount = observers.size();
            if (observerCount == 0) {
                reportResponse("No observers available to process the file.", callback);
                return;
            }

            // Distribuir las líneas entre los observadores
            for (int i = 0; i < lines.size(); i++) {
                CallbackPrx observer = observers.get(i % observerCount);  // Asignar a un observador
                String task = lines.get(i);
                executor.submit(() -> reportResponse("Processing task: " + task, observer));  // Asignar la tarea
            }

            reportResponse("File processing completed.", callback);
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
