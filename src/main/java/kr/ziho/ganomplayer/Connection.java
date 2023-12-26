package kr.ziho.ganomplayer;

import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class Connection {

    private final GANOMPlayer plugin;
    private final Player aiPlayer;
    private final Player realPlayer;
    private Socket socket;
    private SocketAddress address;
    private Thread socketThread;
    private boolean running = false;

    public Connection(GANOMPlayer plugin, Player aiPlayer, Player realPlayer) {
        this.plugin = plugin;
        this.aiPlayer = aiPlayer;
        this.realPlayer = realPlayer;
    }

    public void start() {
        try {
            socket = new Socket();
            address = new InetSocketAddress(plugin.getConfig().getString("host"), plugin.getConfig().getInt("port"));
            socket.connect(address);

            socketThread = new Thread(new SocketThread());
            socketThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            running = true;
            int framesInTimeline = plugin.getConfig().getInt("framesInTimeline");
            int frameInterval = plugin.getConfig().getInt("frameInterval");
            try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                PrintWriter writer = new PrintWriter(out, true);
                double startTime = System.currentTimeMillis();
                while (running) {
                    try {
                        int count = 1;

                        // Receive
                        System.out.println("receiving...");
                        String line = reader.readLine();
                        System.out.println("readLine: " + line);
                        boolean behave = true;
                        JSONArray frames = new JSONArray();
                        try {
                            JSONObject receivedJson = (JSONObject) new JSONParser().parse(line);
                            frames = (JSONArray) receivedJson.get("frames");
                        } catch (ParseException e) {
                            e.printStackTrace();
                            behave = false;
                        }

                        // Create new JSONObject to send
                        JSONObject timelineJson = new JSONObject();
                        timelineJson.put("framesInTimeline", framesInTimeline);
                        timelineJson.put("frameInterval", frameInterval);
                        JSONArray timelineArray = new JSONArray();
                        while (count <= framesInTimeline) {
                            if (System.currentTimeMillis() >= startTime + frameInterval * count) {
                                // Collect data from player
                                timelineArray.add(new PlayerBehavior(realPlayer));
                                // Make AI behave
                                if (behave)
                                    PlayerBehavior.behave(aiPlayer, (JSONObject) frames.get(count - 1));
                                count++;
                            }
                        }

                        startTime = System.currentTimeMillis();

                        timelineJson.put("frames", timelineArray);
                        String outputMessage = timelineJson.toString();
                        aiPlayer.chat(outputMessage);
                        writer.println(outputMessage);
                    } catch (IOException e) {
                        System.out.println("Exception");
                        e.printStackTrace();
                    }
                }
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
