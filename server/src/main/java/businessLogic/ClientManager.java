package businessLogic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import VotingSystem.CallbackPrx;

public class ClientManager {
    // Map to store voters and their respective callback proxies
    Map<String, CallbackPrx> voters = new ConcurrentHashMap<>();

    // Semaphore to control access
    Semaphore semaphore = new Semaphore(1);

    // Observers list
    private final List<CallbackPrx> observers = new CopyOnWriteArrayList<>();

    // Register observer
    public void registerObserver(CallbackPrx observer) {
        observers.add(observer);
        System.out.println("Observer registered");
    }

    // Unregister observer
    public void unregisterObserver(CallbackPrx observer) {
        observers.remove(observer);
        System.out.println("Observer unregistered");
    }

    // Report response
    public void reportResponse(String message, CallbackPrx voter) {
        try {
            voter.reportResponse(message);
        } catch (Exception e) {
            System.err.println("Failed to notify voter: " + e.getMessage());
        }
    }

    // Register voter
    public boolean registerVoter(String voterId, CallbackPrx callback) {
        try {
            semaphore.acquire();
            if (voters.containsKey(voterId)) {
                return false;
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

    // Unregister voter
    public void unRegisterVoter(String voterId) {
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

    // Check connections
    public void checkConnections() {
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
