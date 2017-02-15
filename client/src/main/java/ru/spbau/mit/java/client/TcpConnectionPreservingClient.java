package ru.spbau.mit.java.client;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.BenchmarkStatusCode;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client, which uses tries to establish tcp connection
 * with server
 */
@Slf4j
public class TcpConnectionPreservingClient implements Client {
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
        public Client create() throws IOException {
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
        Request.writeRequest(toSort, out);
        int len = in.readInt();
        byte[] answer = new byte[len];
        in.readFully(answer);
        return IntArrayMsg.parseFrom(answer);
    }

    @Override
    public void disconnect() throws IOException {
        out.writeInt(BenchmarkStatusCode.DISCONNECT);
        connection.close();
    }
}
