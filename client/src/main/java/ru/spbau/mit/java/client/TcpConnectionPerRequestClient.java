package ru.spbau.mit.java.client;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.BenchReqCode;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client, which creates connection on every request
 */
@Slf4j
public class TcpConnectionPerRequestClient implements BenchClient {
    private final String host;
    private final int port;

    public TcpConnectionPerRequestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static class Creator implements ClientCreator {
        private final String host;
        private final int port;

        public Creator(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public BenchClient create() throws IOException {
            return new TcpConnectionPerRequestClient(host, port);
        }
    }

    @Override
    public IntArrayMsg makeBlockingRequest(IntArrayMsg toSort) throws IOException {
        try (Socket connection = new Socket(host, port)) {
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            byte[] msg = toSort.toByteArray();
            out.writeInt(msg.length);
            out.write(msg);
            out.flush();
            int len = in.readInt();
            byte[] answer = new byte[len];
            in.readFully(answer);
//            out.writeInt(BenchReqCode.DISCONNECT);
            return IntArrayMsg.parseFrom(answer);
        }
    }

    @Override
    public void disconnect() throws IOException {
    }
}
