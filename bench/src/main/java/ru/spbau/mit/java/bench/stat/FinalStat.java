package ru.spbau.mit.java.bench.stat;

import lombok.Data;

/**
 * Created by: Egor Gorbunov
 * Date: 12/20/16
 * Email: egor-mailbox@ya.com
 */
@Data
public class FinalStat {
    /**
     * Average time in nano seconds between receiving request and sending response
     */
    private final double avReceiveSendGapNs;
    /**
     * Average time for sorting array (request processing)
     */
    private final double avRequestProcNs;
    /**
     * Average working time of the client
     */
    private final double avClientLifetimeMs;
}
