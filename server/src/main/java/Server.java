
import com.zeroc.Ice.*;
import com.zeroc.Ice.Exception;

import communication.VotingServiceI;

public class Server {
    public static void main(String[] args) {
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapter("VotingService");
            Properties properties = communicator.getProperties();
            Identity id = Util.stringToIdentity(properties.getProperty("Identity"));
            adapter.add(new VotingServiceI(), id);
            adapter.activate();
            System.out.println("Adapter VotingService registered and listening...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
