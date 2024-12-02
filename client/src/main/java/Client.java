import java.io.PrintStream;
import java.util.Scanner;

import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import com.zeroc.IceGrid.QueryPrx;

import communication.CallbackI;

public class Client {
    public static Scanner sc = new Scanner(System.in);
    public static boolean isInterfaceReady = false;
    public static PrintStream originalOut = System.out;
    public static int numRequests = 1;

    public static void main(String[] args) {
        System.setOut(originalOut);
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client")) {
            
            VotingSystem.VotingServicePrx votingServicePrx = null;
            QueryPrx queryPrx = null;
            votingServicePrx = VotingSystem.VotingServicePrx
                .checkedCast(communicator.propertyToProxy("VotingService.Proxy"));     
            if(votingServicePrx == null) {
                queryPrx = QueryPrx.checkedCast(communicator.stringToProxy("DemoIceGrid/Query"));
                votingServicePrx = VotingSystem.VotingServicePrx
                         .checkedCast(queryPrx.findObjectByType("::VotingSystem::VotingService"));                
            }

            if (votingServicePrx == null) {
                throw new RuntimeException("Invalid proxy configuration.");
            }

            ObjectAdapter adapter = communicator.createObjectAdapter("Callback");
            VotingSystem.Callback callback = new CallbackI(votingServicePrx);
            ObjectPrx prx = adapter.add(callback, Util.stringToIdentity("Callback"));
            VotingSystem.CallbackPrx callbackPrx = VotingSystem.CallbackPrx.checkedCast(prx);
            adapter.activate();

            isInterfaceReady = true;

            // Prompt user for role
            boolean isObserver = promptUserRole();
            
            if (isObserver) {
                for(int i=1; i<=50; i++) {
                    try {
                        ObjectPrx proxy = communicator.stringToProxy("VotingService-" + i);
                        votingServicePrx = VotingSystem.VotingServicePrx.checkedCast(proxy);
                        votingServicePrx.registerObserver(callbackPrx);
                    } catch (Exception e) {
                    }
                }
                System.out.println("(System) Registered as an observer. You can use the following commands:");
                System.out.println("""
                        '/upload FILE_PATH' - Upload a file of voter IDs for processing
                        '/exit' - Exit the program"
                        """);

                while (true) {
                    if (handleCitizenCommands(votingServicePrx, callbackPrx, null)) break;
                }
            } else {
                System.out.print("(System) Enter your voter ID: ");
                String voterId = sc.nextLine();

                while (!votingServicePrx.registerVoter(voterId, callbackPrx)) {
                    System.out.println("(System) Voter ID already taken. Enter a different ID.");
                    voterId = sc.nextLine();
                }

                System.out.println("(System) Enter the number of repetitions for the requests: ");
                numRequests = Integer.parseInt(sc.nextLine());

                System.out.println("""
                        (System) Welcome! You can use the following commands:
                        '/get station' - Find your voting station
                        '/list stations CITY' - List voting stations in a city
                        '/upload FILE_PATH' - Upload a file of voter IDs for processing
                        '/exit' - Exit the program"
                        """);

                while (true) {
                    if(queryPrx != null) {
                        votingServicePrx = VotingSystem.VotingServicePrx
                            .checkedCast(queryPrx.findObjectByType("::VotingSystem::VotingService"));
                    }
                    System.out.println("Identity: " + votingServicePrx.ice_getIdentity().name);
                    if (handleCitizenCommands(votingServicePrx, callbackPrx, voterId)) break;
                }
            }

            adapter.destroy();
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean promptUserRole() {
        boolean valid = false;
        String answer = "";

        while (!valid) {
            System.out.print("""
                    Select an option:
                    Register as an observer client (1)
                    Register as a citizen (2)
                    Option:\s""");
            answer = sc.nextLine();
            if (answer.equals("1") || answer.equals("2")) {
                valid = true;
            } else {
                System.out.println("Enter a valid option");
            }
        }
        return answer.equals("1");
    }

    public static boolean handleCitizenCommands(VotingSystem.VotingServicePrx votingServicePrx, VotingSystem.CallbackPrx callbackPrx, String voterId) {
        String input = sc.nextLine();

        if (input.equals("/exit")) {
            if (voterId != null) {
                votingServicePrx.unRegisterVoter(voterId);
            } else {
                votingServicePrx.unregisterObserver(callbackPrx);
                System.out.println("(System) Observer interrupted. Exiting...");
            }
            System.out.println("(System) Exiting... Goodbye!");
            return true; // Stop iteration
        } else if (input.startsWith("/get station")) {
            if (voterId != null) {
                for(int i=0; i<numRequests; i++) {
                    votingServicePrx.getVotingStation(voterId, callbackPrx);
                }
            } else {
                System.out.println("(System) Error: Observers cannot get a voting station.");
            }
        } else if (input.startsWith("/list stations")) {
            String[] parts = input.split(" ", 3);
            if (parts.length == 3) {
                votingServicePrx.listVotingStations(parts[2], callbackPrx);
            } else {
                System.out.println("(System) Error: Please specify a city name.");
            }
        } else if (input.startsWith("/upload")) {
            String[] parts = input.split(" ", 2);
            if (parts.length == 2) {
                votingServicePrx.uploadVoterFile(parts[1], callbackPrx);
            } else {
                System.out.println("(System) Error: Please specify the file path.");
            }
        } else {
            System.out.println("(System) Error: Invalid command.");
        }

        return false; // Start menu again
    }
}