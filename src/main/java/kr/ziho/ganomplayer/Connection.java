package kr.ziho.ganomplayer;

import kr.ziho.ganomplayer.status.PlayerStatusFrame;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connection {

    private final GANOMPlayer plugin;
    private final Player aiPlayer;
    private Socket socket;
    private Thread socketThread;
    private boolean running = false;

    public Connection(GANOMPlayer plugin, Player aiPlayer) {
        this.plugin = plugin;
        this.aiPlayer = aiPlayer;
    }

    public void start() {
        try {
            // socket = new Socket(plugin.getConfig().getString("host"), plugin.getConfig().getInt("port"));
            socketThread = new Thread(new SocketThread());
            socketThread.start();
        } catch (/*IOException*/Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isAlive() {
        return socketThread.isAlive();
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
            try /*(InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream())*/ {
                /*InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);*/
                while (running) {
                    try {
                        // Receive and behave
                        /*
                        String line = bufferedReader.readLine();
                        while (!line.isEmpty()) {
                            System.out.println(line);
                            PlayerData playerData = PlayerData.string2data(line);
                            AIPlayer.behave(aiPlayer, playerData);
                        }
                         */

                        // Collect data from player and send
                        double startTime = System.currentTimeMillis();
                        JSONObject timelineJson = new JSONObject();
                        timelineJson.put("framesInTimeline", framesInTimeline);
                        timelineJson.put("frameInterval", frameInterval);
                        JSONArray timelineArray = new JSONArray();
                        int count = 1;
                        while (count <= framesInTimeline) {
                            if (System.currentTimeMillis() >= startTime + frameInterval * count) {
                                timelineArray.add(new PlayerStatusFrame(aiPlayer));
                                count++;
                            }
                        }
                        timelineJson.put("frames", timelineArray);
                        String outputMessage = timelineJson.toString();  // PlayerData.data2string(AIPlayer.getData(aiPlayer));
                        aiPlayer.chat(outputMessage);
                        // out.write(outputMessage.getBytes(StandardCharsets.UTF_8));
                        // out.flush();
                    } catch (/*IOException*/Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
