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

public class Connection {

    private final GANOMPlayer plugin;
    private final Player aiPlayer;
    private final Player realPlayer;
    private Socket socket;
    private SocketAddress address;
    private boolean running = false;

    public Connection(GANOMPlayer plugin, Player aiPlayer, Player realPlayer) {
        this.plugin = plugin;
        this.aiPlayer = aiPlayer;
        this.realPlayer = realPlayer;
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
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        running = false;
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
                double startTime = System.currentTimeMillis();
                while (running) {
                    try {
                        // Reconnect if connection was lost
                        if (!socket.isConnected())
                            socket.connect(address);

                        int count = 1;

                        // Send first behavior
                        writer.println(new PlayerBehavior(realPlayer, plugin));

                        // Create new JSONObject to send
                        while (count <= framesInTimeline) {  // iterate 10 times
                            if (System.currentTimeMillis() < startTime + frameInterval * count)
                                continue;

                            /* Make AI behave */
                            String line = reader.readLine();
                            // System.out.println("readLine: " + line);
                            JSONArray frames = new JSONArray();
                            try {
                                JSONObject receivedJson = (JSONObject) new JSONParser().parse(line);
                                if (behave)
                                    PlayerBehavior.behave(aiPlayer, receivedJson);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            /* Sending Player Data */
                            String outputMessage = new PlayerBehavior(realPlayer, plugin).toString();
                            // aiPlayer.chat(outputMessage);
                            writer.println(outputMessage);
                            count++;
                        }

                        startTime = System.currentTimeMillis();
                    } catch (IOException e) {
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
