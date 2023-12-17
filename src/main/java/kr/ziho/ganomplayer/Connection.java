package kr.ziho.ganomplayer;

import net.citizensnpcs.api.npc.NPC;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connection {

    private final GANOMPlayer plugin;
    private final NPC aiPlayer;
    private InetSocketAddress ipep;
    private Socket socket;
    private Thread socketThread;
    private boolean running = false;

    public Connection(GANOMPlayer plugin, NPC aiPlayer) {
        this.plugin = plugin;
        this.aiPlayer = aiPlayer;
    }

    public void start() {
        try {
            socket = new Socket(plugin.config.getString("host"), plugin.config.getInt("port"));
            socketThread = new Thread(new SocketThread());
            socketThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SocketThread implements Runnable {
        @Override
        public void run() {
            running = true;
            try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
                while (running) {
                    InputStreamReader inputStreamReader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    try {
                        // Read
                        String line = bufferedReader.readLine();
                        while (!line.isEmpty()) {
                            System.out.println(line);
                            PlayerData playerData = PlayerData.string2data(line);
                            AIPlayer.behave(aiPlayer, playerData);
                        }

                        // Write
                        String outputMessage = PlayerData.data2string(AIPlayer.getData(aiPlayer));
                        out.write(outputMessage.getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
