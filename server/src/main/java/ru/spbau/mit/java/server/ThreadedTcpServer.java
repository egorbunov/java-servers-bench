package ru.spbau.mit.java.server;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.net.ConnectionAcceptor;
import ru.spbau.mit.java.commons.net.OnConnAccept;
import ru.spbau.mit.java.server.stat.OneClientStats;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
            },
            opts.getClientNum()) // max number of connections to accept
        );
    }


    @Override
    public ServerStats bench() {
        try {
            log.info("Waiting for accepting thread to stop accepting clients...");
            acceptingThread.join();
        } catch (InterruptedException e) {
            log.error("Accepting thread join interrupted");
            return null;
        }

        log.info("Waiting for clients...");
        for (Thread ct : clientThreads) {
            try {
                ct.join();
            } catch (InterruptedException e) {
                log.error("Client thread join interrupt");
            }
        }
        ServerStats res = ServerStats.calc(stats);
        log.info("Ok. Returning stats: " + res);
        return res;
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

                while (!Thread.currentThread().isInterrupted()) {
                    int msgLen = in.readInt();

                    if (msgLen < 0) {
                        // disconnect signal!
                        break;
                    }

                    byte[] msg = new byte[msgLen];
                    long startRequest = System.nanoTime();
                    in.readFully(msg);
                    long startSort = System.nanoTime();
                    byte[] answerBytes = RequestProcess.process(msg);
                    long endSort = System.nanoTime();
                    out.writeInt(answerBytes.length);
                    out.write(answerBytes);
                    long endRequest = System.nanoTime();

                    // writing statistics
                    stat.getRequestProcTimes().add(endRequest - startRequest);
                    stat.getSortingTimes().add(endSort - startSort);
                }
            } catch (InvalidProtocolBufferException e) {
                log.error("Protobuf error: " + e.getMessage());
            } catch (IOException e) {
                log.error("IO Error: " + e.getMessage());
            }
        }
    }
}
