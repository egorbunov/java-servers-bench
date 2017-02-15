package ru.spbau.mit.java.server.tcp.simple;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.BenchmarkStatusCode;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.server.RequestProcess;
import ru.spbau.mit.java.server.stat.OneRequestStats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;


@Slf4j
public class OneRequestTask implements Callable<OneRequestStats> {
    private final DataInputStream in;
    private final DataOutputStream out;

    public OneRequestTask(DataInputStream in, DataOutputStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * @return null in case end of connection!
     */
    @Override
    public OneRequestStats call() throws IOException {
        int msgLen = in.readInt();
        if (msgLen == BenchmarkStatusCode.DISCONNECT) {
            // disconnect signal!
            return null;
        }
        byte[] msg = new byte[msgLen];
        long startRequest = System.nanoTime();
        in.readFully(msg);
        IntArrayMsg arrToSort = IntArrayMsg.parseFrom(msg);

        long startProc = System.nanoTime();
        IntArrayMsg sorted = RequestProcess.processArray(arrToSort);
        long endProc = System.nanoTime();

        byte[] ans = sorted.toByteArray();
        out.writeInt(ans.length);
        out.write(ans);
        long endRequest = System.nanoTime();
        return new OneRequestStats(
                endRequest - startRequest,
                endProc - startProc
        );
    }
}
