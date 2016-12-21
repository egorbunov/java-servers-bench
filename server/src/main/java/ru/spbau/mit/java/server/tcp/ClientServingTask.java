package ru.spbau.mit.java.server.tcp;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.server.stat.OneRequestStats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * One client "session", which calculates stats
 */
@Slf4j
class ClientServingTask implements Callable<List<OneRequestStats>> {
    private final Socket clientSock;

    ClientServingTask(Socket clientSock) {
        this.clientSock = clientSock;
    }

    @Override
    public List<OneRequestStats> call() throws Exception {
        List<OneRequestStats> stats = new ArrayList<>();

        try (Socket sock = clientSock) {
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            DataInputStream in = new DataInputStream(sock.getInputStream());

            while (!Thread.currentThread().isInterrupted()) {
                OneRequestTask oneRequestTask = new OneRequestTask(in, out);
                OneRequestStats stat = oneRequestTask.call();
                if (stat == null) {
                    break;
                }
                stats.add(stat);
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Protobuf error: " + e.getMessage());
            throw new IOError(e);
        } catch (IOException e) {
            log.error("IO Error: " + e.getMessage());
            throw new IOError(e);
        }
        return stats;
    }
}
