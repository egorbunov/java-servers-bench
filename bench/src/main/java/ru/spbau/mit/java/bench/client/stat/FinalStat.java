package ru.spbau.mit.java.bench.client.stat;

import lombok.Data;

/**
 * Created by: Egor Gorbunov
 * Date: 12/20/16
 * Email: egor-mailbox@ya.com
 */
@Data
public class FinalStat {
    /**
     * Average time in nano seconds for request processing (read + sort + write)
     */
    private final double avRequestNs;
    /**
     * Average time for sorting array
     */
    private final double avSortNs;
    /**
     * Average working time of the client
     */
    private final double avClientLifetimeNs;
}
