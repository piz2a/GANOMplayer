package kr.ziho.ganomplayer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

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

        System.out.println("Connection Type: " + (mirrorTest ? "Mirror Test" : "Training"));

        running = true;
        Thread socketThread = new Thread(new SocketThread());
        socketThread.start();
    }

    public void stop() {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("-1");
            running = false;
            // socket.close();
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
            // running = true;
            // int framesInTimeline = plugin.getConfig().getInt("framesInTimeline");
            int frameInterval = plugin.getConfig().getInt("frameInterval");
            try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
                // BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
                Scanner reader = new Scanner(in);
                PrintWriter writer = new PrintWriter(out, true);
                int serverDownTimer = 0;

                /* Sending First Player Data */
                Location initLocation = realPlayer.getLocation();
                plugin.locationMap.put(realPlayer.getUniqueId(), initLocation);
                writer.println(new PlayerBehavior(realPlayer, plugin, initLocation));

                double startTime = System.currentTimeMillis();
                double timestamp = startTime;
                while (running) {
                    // Reconnect if connection was lost
                    // if (!socket.isConnected()) socket.connect(address);

                    // Wait
                    while (System.currentTimeMillis() < startTime + frameInterval);
                    double timestamp2 = System.currentTimeMillis();
                    System.out.println("Waiting interval: " + (timestamp2 - timestamp));

                    startTime += frameInterval;  // Here the period ends

                    /* Make AI behave */
                    String line = reader.hasNextLine() ? reader.nextLine() : null;
                    System.out.println("Receiving interval: " + (System.currentTimeMillis() - timestamp2));
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
                    if (serverDownTimer > 30)
                        running = false;

                    /* Sending Player Data */
                    Location prevLocation = plugin.locationMap.get(realPlayer.getUniqueId());
                    String outputMessage = new PlayerBehavior(realPlayer, plugin, prevLocation).toString();
                    // aiPlayer.chat(outputMessage);
                    writer.println(outputMessage);
                    plugin.locationMap.replace(realPlayer.getUniqueId(), realPlayer.getLocation());
                    timestamp = System.currentTimeMillis();  // Time right after sending data
                }
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
