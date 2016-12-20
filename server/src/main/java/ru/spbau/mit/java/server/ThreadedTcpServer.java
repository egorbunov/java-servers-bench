package ru.spbau.mit.java.server;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.net.ConnectionAcceptor;
import ru.spbau.mit.java.commons.net.OnConnAccept;
import ru.spbau.mit.java.server.stat.OneClientStats;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Server, which creates one thread per client and uses tcp sockets for
 * communication with them
 */
@Slf4j
public class ThreadedTcpServer implements BenchServer {
    private final ServerSocket serverSocket;
    private final BenchOpts opts;
    private final Thread acceptingThread;
    private final List<Thread> clientThreads;
    private final List<OneClientStats> stats;

    public ThreadedTcpServer(int port, BenchOpts opts) throws IOException {
        serverSocket = new ServerSocket(port);
        this.opts = opts;
        clientThreads = new ArrayList<>(opts.getClientNum());
        stats = new ArrayList<>(opts.getClientNum());

        acceptingThread = new Thread(new ConnectionAcceptor(serverSocket, clientSock -> {
            // handle connection accepted event
            OneClientStats stat = new OneClientStats();
            Thread t = new Thread(new ClientServingTask(clientSock, stat));
            stats.add(stat);
            clientThreads.add(t);
            t.start();
        }));
    }


    @Override
    public ServerStats bench() {
        start();
        try {
            acceptingThread.join();
        } catch (InterruptedException e) {
            log.error("Accepting thread join interrupted");
            return null;
        }

        for (Thread ct : clientThreads) {
            try {
                ct.join();
            } catch (InterruptedException e) {
                log.error("Client thread join interrupt");
            }
        }

        double avRequestMs = stats.stream()
                .mapToDouble(x -> x.getRequestProcTimes().stream().mapToLong(it -> it).average().orElse(0.0))
                .average().orElse(0.0);
        double avSortingMs = stats.stream()
                .mapToDouble(x -> x.getSortingTimes().stream().mapToLong(it -> it).average().orElse(0.0))
                .average().orElse(0.0);

        return new ServerStats(avRequestMs, avSortingMs);
    }

    @Override
    public void start() {
        acceptingThread.start();
    }

    @Override
    public void stop() throws InterruptedException, IOException {
        acceptingThread.interrupt();
        acceptingThread.join();
        for (Thread ct : clientThreads) {
            ct.interrupt();
            ct.join();
        }
        serverSocket.close();
    }

    private static class ClientServingTask implements Runnable {
        private final Socket clientSock;
        private final OneClientStats stat;

        ClientServingTask(Socket clientSock, OneClientStats stat) {
            this.clientSock = clientSock;
            this.stat = stat;
        }

        @Override
        public void run() {
            try (Socket sock = clientSock) {
                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                DataInputStream in = new DataInputStream(sock.getInputStream());
                byte[] msg = null;

                while (!Thread.currentThread().isInterrupted()) {
                    int msgLen = in.readInt();
                    if (msg == null) {
                        // we rely on that in one session (from connect to disconnect)
                        // client may only send equal arrays
                        msg = new byte[msgLen];
                    }

                    long startRequest = System.currentTimeMillis();
                    in.readFully(msg);
                    long startSort = System.currentTimeMillis();
                    byte[] answerBytes = RequestProcess.process(msg);
                    long endSort = System.currentTimeMillis();
                    out.writeInt(answerBytes.length);
                    out.write(answerBytes);
                    long endRequest = System.currentTimeMillis();

                    // writing statistics
                    stat.getRequestProcTimes().add(endRequest - startRequest);
                    stat.getSortingTimes().add(endSort - startSort);
                }
            } catch (IOException e) {
                log.error("Socket error: " + e.getMessage());
            }
        }
    }
}
