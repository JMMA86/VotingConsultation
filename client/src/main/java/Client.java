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
            Demo.ChatPrx chatManagerPrx = Demo.ChatPrx
                    .checkedCast(communicator.propertyToProxy("Chat.Proxy"));

            try {

                ObjectAdapter adapter = communicator.createObjectAdapter("Callback");
                Demo.Callback callback = new CallbackI();

                ObjectPrx prx = adapter.add(callback, Util.stringToIdentity("Callback"));
                Demo.CallbackPrx callbackPrx = Demo.CallbackPrx.checkedCast(prx);
                adapter.activate();
                isInterfaceReady = true;

                // Register user
                System.out.print("(System) Enter your username: ");
                String username = sc.nextLine();

                // Check if username is already taken
                while (!chatManagerPrx.registerUser(username, callbackPrx)) {
                    System.out.println("(System) Username already taken. Please enter a different username.");
                    System.out.print("(System) Enter your username: ");
                    username = sc.nextLine();
                }

                // Welcome message
                System.out.println("(System) Welcome " + username + "!" +
                        " Type '/list clients' to list all clients, " +
                        "'/exit' to exit, " +
                        "'to X: message' to send a message to user X, " +
                        "and 'BC: message' to broadcast a message." +
                        "\n---------------CHAT---------------");

                /*
                 * System.out.println("Waiting for response...");
                 * long start = System.currentTimeMillis();
                 * chatManagerPrx.printString("Hello World");
                 * chatManagerPrx.fact(20, callbackPrx);
                 * // System.out.println("Factorial of 10 is: " );
                 * System.out.println("Time taken: " + (System.currentTimeMillis() - start) +
                 * "ms");
                 *
                 */

                // Obtaining messages
                while (true) {
                    System.out.print(bufferStream.toString());
                    bufferStream.reset();
                    String input = sc.nextLine();
                    if (input.charAt(0) == '/') {
                        try {
                            if (input.equals("/list clients")) {
                                chatManagerPrx.listClients(username);
                            } else if (input.equals("/exit")) {
                                chatManagerPrx.unRegisterUser(username);
                                System.out.println("(System) Exiting chat...");
                                break;
                            } else if (input.startsWith("/fib")) {
                                String[] parts = input.split(" ");
                                long n = Long.parseLong(parts[1]);
                                chatManagerPrx.fib(n, callbackPrx);
                            } else if (input.startsWith("/listports")) {
                                String[] parts = input.split(" ");
                                String ip = parts[1];
                                chatManagerPrx.openPorts(ip, callbackPrx);
                            } else if (input.equals("/listifs")) {
                                chatManagerPrx.networkInterfaces(callbackPrx);
                            } else {
                                System.out.println("(System) Error: Invalid command.");
                            }
                        } catch (Exception e) {
                            System.out.println("(System) Error: Invalid command or input.");
                        }
                    } else if (input.contains(":") && input.charAt(0) == 't' && input.charAt(1) == 'o') {
                        String[] parts = input.split(":");
                        String destUser = parts[0].trim().substring(3);
                        String message = parts[1].trim();
                        chatManagerPrx.sendMessage(message, username, destUser);
                    } else if (input.charAt(0) == 'B' && input.charAt(1) == 'C' && input.charAt(2) == ':') {
                        String[] parts = input.split(":");
                        String message = parts[1].trim();
                        chatManagerPrx.broadCastMessage(message, username);
                    } else if (input.charAt(0) == '!') {
                        chatManagerPrx.executeCommand(input.substring(1), callbackPrx);
                    } else {
                        System.out.println("(System) Error: Invalid input.");
                    }
                }

                //Shut down
                adapter.destroy();
                sc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
