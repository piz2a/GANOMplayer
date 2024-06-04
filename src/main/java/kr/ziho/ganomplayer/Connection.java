package kr.ziho.ganomplayer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.*;

public class Connection {

    private final GANOMPlayer plugin;
    private final Player aiPlayer;
    private final Player realPlayer;
    private final boolean mirrorTest;
    private Socket socket;
    private SocketAddress address;
    private boolean running = false;

    public Connection(GANOMPlayer plugin, Player aiPlayer, Player realPlayer, boolean mirrorTest) {
        this.plugin = plugin;
        this.aiPlayer = aiPlayer;
        this.realPlayer = realPlayer;  // if mirror test: realPlayer = aiPlayer.
        this.mirrorTest = mirrorTest;
    }

    public void start() throws IOException {
        socket = new Socket();
        address = new InetSocketAddress(plugin.getConfig().getString("host"), plugin.getConfig().getInt("port"));
        socket.connect(address);

        Thread socketThread = new Thread(new SocketThread());
        socketThread.start();
    }

    public void stop() {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("-1");
            running = false;
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public String getAIName() {
        return aiPlayer.getName();
    }

    private class SocketThread implements Runnable {

        boolean behave = true;  // false;  // Now forge mod makes ai player behave

        @Override
        public void run() {
            running = true;
            int framesInTimeline = plugin.getConfig().getInt("framesInTimeline");
            int frameInterval = plugin.getConfig().getInt("frameInterval");
            try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in), 4096);
                PrintWriter writer = new PrintWriter(out, true);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                double startTime = System.currentTimeMillis();
                int serverDownTimer = 0;

                /* Sending First Player Data */
                Location initLocation = realPlayer.getLocation();
                plugin.locationMap.put(realPlayer.getUniqueId(), initLocation);
                writer.println(new PlayerBehavior(realPlayer, plugin, initLocation));

                while (running) {
                    try {
                        // Reconnect if connection was lost
                        if (!socket.isConnected())
                            socket.connect(address);

                        int count = 1;

                        // Send first behavior: writer.println(new PlayerBehavior(realPlayer, plugin));

                        // Create new JSONObject to send
                        while (count <= framesInTimeline) {  // iterate 10 times
                            // This code should be at the top of the loop
                            if (System.currentTimeMillis() < startTime + frameInterval * count)
                                continue;

                            /* Make AI behave */
                            String line = null, newLine;
                            while (true) {
                                Callable<String> readLineTask = () -> {
                                    try {
                                        return reader.readLine();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                };

                                Future<String> future = executor.submit(readLineTask);
                                try {
                                    newLine = future.get(frameInterval / 2, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                    System.out.println("Readline timeout");
                                    break;
                                }
                                System.out.println("newLine: " + newLine);
                                if (newLine.isEmpty()) break;
                                line = newLine;
                            }

                            try {
                                if (line == null) {
                                    // this means socket server is down
                                    serverDownTimer++;
                                } else {
                                    serverDownTimer = 0;
                                    System.out.println("readLine: " + line);
                                    JSONObject receivedJson = (JSONObject) new JSONParser().parse(line);
                                    if (behave)
                                        PlayerBehavior.behave(aiPlayer, receivedJson, mirrorTest);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            /* Sending Player Data */
                            Location prevLocation = plugin.locationMap.get(realPlayer.getUniqueId());
                            String outputMessage = new PlayerBehavior(realPlayer, plugin, prevLocation).toString();
                            // aiPlayer.chat(outputMessage);
                            writer.println(outputMessage);
                            plugin.locationMap.replace(realPlayer.getUniqueId(), realPlayer.getLocation());

                            count++;
                        }
                        if (serverDownTimer > 30)
                            running = false;

                        startTime = System.currentTimeMillis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                socket.close();  // DO NOT CLOSE
                executor.shutdown();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
