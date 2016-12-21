package ru.spbau.mit.java.client;

import ru.spbau.mit.java.commons.BenchReqCode;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client, which uses tries to establish tcp connection
 * with server
 */
public class TcpConnectionPreservingClient implements BenchClient {
    private final Socket connection;
    private final DataOutputStream out;
    private final DataInputStream in;

    /**
     * Tcp client factory
     */
    public static class Creator implements ClientCreator {
        private final String host;
        private final int port;

        public Creator(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public BenchClient create() throws IOException {
            return new TcpConnectionPreservingClient(host, port);
        }
    }

    public TcpConnectionPreservingClient(String host, int port) throws IOException {
        connection = new Socket(host, port);
        in = new DataInputStream(connection.getInputStream());
        out = new DataOutputStream(connection.getOutputStream());
    }

    @Override
    public IntArrayMsg makeBlockingRequest(IntArrayMsg toSort) throws IOException {
        byte[] msg = toSort.toByteArray();
        out.writeInt(msg.length);
        out.write(msg);
        out.flush();
        int len = in.readInt();
        byte[] answer = new byte[len];
        in.readFully(answer);
        return IntArrayMsg.parseFrom(answer);
    }

    @Override
    public void disconnect() throws IOException {
        out.writeInt(BenchReqCode.DISCONNECT);
        connection.close();
    }
}
