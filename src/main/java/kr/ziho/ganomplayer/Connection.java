package kr.ziho.ganomplayer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
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
    private final Player scannedPlayer;
    private final boolean mirrorTest;
    private final int PORT = 25567;
    private Socket socket, modSocket;
    private boolean running = false;

    public Connection(GANOMPlayer plugin, Player aiPlayer, Player scannedPlayer, boolean mirrorTest) {
        this.plugin = plugin;
        this.aiPlayer = aiPlayer;
        this.scannedPlayer = scannedPlayer;  // if mirror test: scannedPlayer = aiPlayer.
        this.mirrorTest = mirrorTest;
    }

    public void start() throws IOException {
        socket = new Socket();
        SocketAddress address = new InetSocketAddress(plugin.getConfig().getString("host"), plugin.getConfig().getInt("port"));
        socket.connect(address);

        // Socket for AI
        modSocket = new Socket();
        InetSocketAddress modAddress = new InetSocketAddress("127.0.0.1", PORT);
        modSocket.connect(modAddress);

        // Socket for real player
        int index = 1;
        for (Player opponent : aiPlayer.getWorld().getPlayers()) {
            if (opponent.equals(aiPlayer)) continue;
            Socket opponentSocket = new Socket();
            int opponentPort = PORT + index++;
            opponentSocket.connect(new InetSocketAddress("127.0.0.1", opponentPort));
            System.out.println("Connecting port " + opponentPort + " for player " + opponent.getName());
            plugin.socketMap.put(opponent.getUniqueId(), opponentSocket);
        }

        System.out.println("Connection Type: " + (mirrorTest ? "Mirror Test" : "Training"));

        running = true;
        Thread socketThread = new Thread(new SocketThread());
        socketThread.start();
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public String getAIName() {
        return aiPlayer.getName();
    }

    private class SocketThread implements Runnable {

        @Override
        public void run() {
            // running = true;
            // int framesInTimeline = plugin.getConfig().getInt("framesInTimeline");
            int frameInterval = plugin.getConfig().getInt("frameInterval");
            boolean isDebug = plugin.getConfig().getBoolean("debug");
            try (
                InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream();
                InputStream modIn = modSocket.getInputStream(); OutputStream modOut = modSocket.getOutputStream();
            ) {
                // BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
                Scanner reader = new Scanner(in);
                PrintWriter writer = new PrintWriter(out, true);
                int serverDownTimer = 0;

                /* Sending First Player Data */
                Location initLocation = scannedPlayer.getLocation();
                plugin.locationMap.put(scannedPlayer.getUniqueId(), initLocation);
                for (Player opponent : aiPlayer.getWorld().getPlayers()) {
                    plugin.locationMap.put(opponent.getUniqueId(), opponent.getLocation());
                }
                writer.println(getOutputJson(initLocation, modIn, modOut));

                double startTime = System.currentTimeMillis();
                double timestamp = startTime;
                while (running) {
                    // Reconnect if connection was lost
                    // if (!socket.isConnected()) socket.connect(address);

                    // Wait
                    while (System.currentTimeMillis() < startTime + frameInterval);
                    double timestamp2 = System.currentTimeMillis();
                    if (isDebug) System.out.println("Waiting interval: " + (timestamp2 - timestamp));

                    startTime += frameInterval;  // Here the period ends

                    /* If player has logged out: stops training: Incomplete */

                    /* Make AI behave */
                    String line = reader.hasNextLine() ? reader.nextLine() : null;
                    if (isDebug) System.out.println("Receiving interval: " + (System.currentTimeMillis() - timestamp2));
                    try {
                        if (line == null) {
                            // this means socket server is down
                            serverDownTimer++;
                        } else {
                            serverDownTimer = 0;
                            if (isDebug) System.out.println("readLine: " + line);
                            JSONObject receivedJson = (JSONObject) new JSONParser().parse(line);
                            PlayerBehavior.behave(aiPlayer, receivedJson, modOut, mirrorTest);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (serverDownTimer > 30)
                        running = false;

                    /* Sending Player Data */
                    Location prevLocation = plugin.locationMap.get(scannedPlayer.getUniqueId());
                    JSONObject outputJson = getOutputJson(prevLocation, modIn, modOut);
                    String outputMessage = outputJson.toString();
                    // aiPlayer.chat(outputMessage);
                    writer.println(outputMessage);
                    plugin.locationMap.replace(scannedPlayer.getUniqueId(), scannedPlayer.getLocation());
                    for (Player opponent : aiPlayer.getWorld().getPlayers()) {
                        plugin.locationMap.replace(opponent.getUniqueId(), opponent.getLocation());
                    }
                    timestamp = System.currentTimeMillis();  // Time right after sending data
                }
                writer.println("-1");
                socket.close();
                System.out.println("Training with " + aiPlayer.getName() + " has stopped.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    JSONObject getOutputJson(Location prevLocation, InputStream modIn, OutputStream modOut) throws IOException {
        // return new PlayerBehavior(scannedPlayer, plugin, prevLocation, true);
        JSONObject outputJson = new JSONObject();
        outputJson.put("ai", new PlayerBehavior(scannedPlayer, plugin, requestKeyLog(modIn, modOut), prevLocation, true));
        outputJson.put("players", new JSONArray() {{
            for (Player opponent : aiPlayer.getWorld().getPlayers()) {
                if (opponent.getUniqueId() == scannedPlayer.getUniqueId())
                    continue;
                Socket opponentSocket = plugin.socketMap.get(opponent.getUniqueId());
                InputStream opponentIn = opponentSocket.getInputStream();
                OutputStream opponentOut = opponentSocket.getOutputStream();
                add(new PlayerBehavior(opponent, plugin, requestKeyLog(opponentIn, opponentOut), plugin.locationMap.get(opponent.getUniqueId()), true));
            }
        }});
        return outputJson;
    }

    private int requestKeyLog(InputStream in, OutputStream out) {
        PrintWriter modWriter = new PrintWriter(out, true);
        modWriter.println("keylog");
        Scanner modScanner = new Scanner(in);
        while (!modScanner.hasNextLine());  // Wait
        String line = modScanner.nextLine();
        int Space = line.charAt(0) == '1' ? 1 : 0;
        int WSmove = (line.charAt(1) == '1' ? 1 : 0) - (line.charAt(3) == '1' ? 1 : 0);
        int ADmove = (line.charAt(2) == '1' ? 1 : 0) - (line.charAt(4) == '1' ? 1 : 0);
        return Space * 4 + WSmove * 2 + ADmove;
    }

}
