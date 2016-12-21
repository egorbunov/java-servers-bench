package ru.spbau.mit.java.client;

import org.apache.commons.io.IOUtils;
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
public class TcpClient implements BenchClient {
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
            return new TcpClient(host, port);
        }
    }

    public TcpClient(String host, int port) throws IOException {
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
        assert len == msg.length;
        in.readFully(msg);
        return IntArrayMsg.parseFrom(msg);
    }

    @Override
    public void disconnect() throws IOException {
        out.writeInt(BenchReqCode.DISCONNECT);
        in.close();
        out.close();
        connection.close();
    }
}
