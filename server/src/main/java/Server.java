public class Server {
    public static void main(String[] args) {
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server")) {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("VotingService");
            com.zeroc.Ice.Object object = new VotingServiceI();
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("VotingService.Proxy"));
            adapter.activate();
            communicator.waitForShutdown();
        }
    }
}
