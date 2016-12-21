package ru.spbau.mit.java.server.tcp;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.BenchReqCode;
import ru.spbau.mit.java.server.RequestProcess;
import ru.spbau.mit.java.server.stat.OneClientStats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * One client "session", which calculates stats
 */
@Slf4j
class ClientServingTask implements Callable<OneClientStats> {
    private final Socket clientSock;

    ClientServingTask(Socket clientSock) {
        this.clientSock = clientSock;
    }

    @Override
    public OneClientStats call() throws Exception {
        OneClientStats stat = new OneClientStats();

        try (Socket sock = clientSock) {
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            DataInputStream in = new DataInputStream(sock.getInputStream());

            while (!Thread.currentThread().isInterrupted()) {
                int msgLen = in.readInt();

                if (msgLen == BenchReqCode.DISCONNECT) {
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
            throw new IOError(e);
        } catch (IOException e) {
            log.error("IO Error: " + e.getMessage());
            throw new IOError(e);
        }

        return stat;
    }
}
