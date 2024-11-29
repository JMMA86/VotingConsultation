import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zeroc.Ice.Current;

import Demo.CallbackPrx;

public class ChatI implements Demo.Chat {
    // Map to store users and their respective callback proxies
    Map<String, CallbackPrx> users = new HashMap<>();
    // Map to store pending messages for each user
    Map<String, List<String>> pendingMessages = new HashMap<>();
    // Semaphore to control map access
    Semaphore semaphore = new Semaphore(1);
    // ThreadPool to handle factorial and fibonacci requests
    ExecutorService executor = Executors.newFixedThreadPool(5);

    public ChatI() {
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
    public boolean registerUser(String username, CallbackPrx callback, Current current) {
        try {
            semaphore.acquire();
            username = username.trim();

            if (users.containsKey(username)) {
                return false;
            }

            if (pendingMessages.containsKey(username)) {
                // User is reconnecting, store callback and mark for pending message delivery
                users.put(username, callback);
                System.out.println("User " + username + " reconnected");

                // Deliver pending messages
                List<String> messages = pendingMessages.get(username);
                if (messages != null) {
                    callback.reportResponse("(System) Pending messages:");
                    for (String message : messages) {
                        callback.reportResponse(message);
                    }
                    messages.clear(); // Clear the messages after delivering
                }
            } else {
                users.put(username, callback);
                pendingMessages.put(username, new ArrayList<>());
                System.out.println("User " + username + " registered");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void unRegisterUser(String username, Current current) {
        try {
            semaphore.acquire();
            users.remove(username);
            System.out.println("User " + username + " unregistered");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void openPorts(String ipAddress, Demo.CallbackPrx callback, Current current) {
        executor.submit(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("\nOpen ports:\n");
            for(int port=1; port<=65535; port++) {
                try {
                    java.net.Socket socket = new java.net.Socket(ipAddress, port);
                    sb.append(port).append("\n");
                    socket.close();
                } catch (java.io.IOException e) {
                }
            }
            callback.reportResponse(sb.toString());
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void executeCommand(String command, Demo.CallbackPrx callback, Current current) {
        executor.submit(() -> {
            StringBuilder ans = new StringBuilder();
            Process process;
            try {
                process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
                String line;
                while ((line = reader.readLine()) != null) {
                    ans.append(line).append("\n");
                }
        
                process.waitFor();
                callback.reportResponse(ans.toString());
            } catch (IOException | InterruptedException  e) {
                callback.reportResponse("Error: " + e.getMessage());
            }
        });
    }

    @Override
    public void networkInterfaces(Demo.CallbackPrx callback, Current current) {
        executor.submit(() -> {
            StringBuilder output = new StringBuilder();
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                int num = 0;
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    output.append("Interface " + num + ": " + networkInterface.getDisplayName() + "\n");
                    num++;
                }
            } catch (SocketException e) {
                callback.reportResponse("Error: " + e.getMessage());
            }
            callback.reportResponse(output.toString());
        });
    }

    @Override
    public void listClients(String username, Current current) {
        executor.submit(() -> {
            System.out.println(users.size());
            CallbackPrx callback = users.get(username);
            StringBuilder sb = new StringBuilder();
            sb.append("\nList of clients:\n");
            int cnt = 1;
            for (String user : users.keySet()) {
                sb.append("(" + cnt + "): " + user + "\n");
                cnt++;
            }
            callback.reportResponse(sb.toString());
        });
    }

    @Override
    public void sendMessage(String s, String fromUser, String destUser, Current current) {
        executor.submit(() -> {
            CallbackPrx destPrx = users.get(destUser);
            CallbackPrx fromPrx = users.get(fromUser);
            if (destPrx != null && fromPrx != destPrx) {
                destPrx.reportResponse(fromUser + ": " + s);
            } else if (fromPrx == destPrx) {
                fromPrx.reportResponse("You cannot send message to yourself");
            } else {
                fromPrx.reportResponse("User " + destUser + " is currently offline. Your message will be delivered when they reconnect.");
                // Store the message in pending messages
                List<String> messages = pendingMessages.get(destUser);
                if (messages != null) {
                    messages.add(fromUser + ": " + s);
                } else {
                    messages = new ArrayList<>();
                    messages.add(fromUser + ": " + s);
                    pendingMessages.put(destUser, messages);
                }
            }
        });
    }

    @Override
    public void broadCastMessage(String s, String fromUser, Current current) {
        executor.submit(() -> {
            for (String user : users.keySet()) {
                if (!user.equals(fromUser)) {
                    CallbackPrx destPrx = users.get(user);
                    destPrx.reportResponse(fromUser + ": " + s);
                }
            }
        });
    }

    public void printString(String msg, com.zeroc.Ice.Current current) {
        System.out.println(msg);
    }

    public void checkConnections() {
        for (String user : new ArrayList<>(users.keySet())) {
            CallbackPrx callback = users.get(user);
            try {
                callback.ice_ping();
            } catch (Exception e) {
                users.remove(user);
                System.out.println("User " + user + " disconnected");
            }
        }
    }

    public void fact(long n, Demo.CallbackPrx callback, com.zeroc.Ice.Current current) {
        BigInteger fact = BigInteger.ONE;
        for (long i = 1; i <= n; i++) {
            fact = fact.multiply(BigInteger.valueOf(i));
        }
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            // TODO: handle exception
        }
        String response = "Factorial of " + n + " is: " + fact;
        callback.reportResponse(response);
    }

    public void fib(long num, Demo.CallbackPrx callback, com.zeroc.Ice.Current current) {
        long n = num;
        if (n <= 0) {
            callback.reportResponse("0");
        }
        StringBuilder output = new StringBuilder();
        long a = 0, b = 1;
        // output.append(a).append(" ");
        if (n < 2) {
            output.append(n).append(" ");
        }
        for (long i = 2; i < n; i++) {
            long c = a + b;
            // System.out.println(c);
            a = b;
            b = c;
            if(i == n-1) output.append(c).append(" ");
        }
        Set<Long> factors = new HashSet<>();
        int factorsCount = 0;
        long i = 2;
        while (n > 1) {
            if (n % i == 0) {
                // factors.add(i);
                factorsCount++;
                n /= i;
            } else {
                i++;
            }
        }
        for (Long factor : factors) {
            output.append(factor).append(" ");
        }
        output.append(factorsCount);
        callback.reportResponse(output.toString().trim());
    }
}