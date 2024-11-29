public class Server {
    public static void main(String[] args) {
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server")) {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Chat");
            com.zeroc.Ice.Object object = new ChatI();
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("Chat.Proxy"));
            adapter.activate();
            communicator.waitForShutdown();
        }
    }
}