import communication.VotingServiceI;

public class Server {
    public static void main(String[] args) {
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server")) {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("VotingService");
            VotingServiceI servant = new VotingServiceI();
            adapter.add(servant, com.zeroc.Ice.Util.stringToIdentity("VotingService"));
            adapter.activate();
            System.out.println("Adapter VotingService registered and listening...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
