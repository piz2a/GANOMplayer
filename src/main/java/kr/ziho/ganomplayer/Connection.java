package kr.ziho.ganomplayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Connection {

    private GANOMPlayer plugin;
    private InetSocketAddress ipep;
    private Socket socket;
    protected OutputStream sender;
    private InputStream receiver;
    private Thread socketThread;

    public Connection(GANOMPlayer plugin, Socket socket) {
        this.plugin = plugin;
        this.socket = socket;
        this.init();
    }

    private void init() {
        ipep = new InetSocketAddress("127.0.0.1", 25566);
        try {
            socket.connect(ipep);
            sender = socket.getOutputStream();
            receiver = socket.getInputStream();
            socketThread = new Thread(new SocketThread());
            socketThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SocketThread implements Runnable {
        @Override
        public void run() {
            try {
                // 전송할 메시지를 작성한다.
                String msg = "java test message - ";
                // string을 byte배열 형식으로 변환한다.
                byte[] data = msg.getBytes();
                // ByteBuffer를 통해 데이터 길이를 byte형식으로 변환한다.
                ByteBuffer b = ByteBuffer.allocate(4);
                // byte포멧은 little 엔디언이다.
                b.order(ByteOrder.LITTLE_ENDIAN);
                b.putInt(data.length);
                // 데이터 길이 전송
                sender.write(b.array(), 0, 4);
                // 데이터 전송
                sender.write(data);
                data = new byte[4];
                // 데이터 길이를 받는다.
                receiver.read(data, 0, 4);
                // ByteBuffer를 통해 little 엔디언 형식으로 데이터 길이를 구한다.
                ByteBuffer b2 = ByteBuffer.wrap(data);
                b2.order(ByteOrder.LITTLE_ENDIAN);
                int length = b2.getInt();
                // 데이터를 받을 버퍼를 선언한다.
                data = new byte[length];
                // 데이터를 받는다.
                receiver.read(data, 0, length);
                // byte형식의 데이터를 string형식으로 변환한다.
                msg = new String(data, StandardCharsets.UTF_8);
                // 콘솔에 출력한다.
                System.out.println(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
