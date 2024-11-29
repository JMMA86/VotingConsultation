import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

public class Client {
    public static Scanner sc = new Scanner(System.in);
    public static boolean isInterfaceReady = false;
    public static ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
    public static PrintStream originalOut = System.out;

    public static void main(String[] args) {
        System.setOut(originalOut);
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client")) {
            VotingSystem.VotingServicePrx votingServicePrx = VotingSystem.VotingServicePrx
                .checkedCast(communicator.propertyToProxy("VotingService.Proxy"));

            if (votingServicePrx == null) {
                throw new RuntimeException("Invalid proxy configuration.");
            }

            ObjectAdapter adapter = communicator.createObjectAdapter("Callback");
            VotingSystem.Callback callback = new CallbackI();
            ObjectPrx prx = adapter.add(callback, Util.stringToIdentity("Callback"));
            VotingSystem.CallbackPrx callbackPrx = VotingSystem.CallbackPrx.checkedCast(prx);
            adapter.activate();

            isInterfaceReady = true;

            // Register voter
            System.out.print("(System) Enter your voter ID: ");
            String voterId = sc.nextLine();

            while (!votingServicePrx.registerVoter(voterId, callbackPrx)) {
                System.out.println("(System) Voter ID already taken. Enter a different ID.");
                voterId = sc.nextLine();
            }

            System.out.println("(System) Welcome! You can use the following commands:\n" +
                    "'/get station' - Find your voting station\n" +
                    "'/list stations CITY' - List voting stations in a city\n" +
                    "'/exit' - Exit the program");

            // Main input loop
            while (true) {
                System.out.print("Enter command: ");
                String input = sc.nextLine();
                
                if (input.equals("/exit")) {
                    votingServicePrx.unRegisterVoter(voterId);
                    System.out.println("(System) Exiting... Goodbye!");
                    break;
                } else if (input.startsWith("/get station")) {
                    votingServicePrx.getVotingStation(voterId, callbackPrx);
                } else if (input.startsWith("/list stations")) {
                    String[] parts = input.split(" ", 3);
                    if (parts.length == 3) {
                        votingServicePrx.listVotingStations(parts[2], callbackPrx);
                    } else {
                        System.out.println("(System) Error: Please specify a city name.");
                    }
                } else {
                    System.out.println("(System) Error: Invalid command.");
                }
            }

            adapter.destroy();
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
