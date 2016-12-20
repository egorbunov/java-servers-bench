package ru.spbau.mit.java.server.stat;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Statistics
 */
@Data
public class ServerStats {
    public static ServerStats calc(List<OneClientStats> clientStats) {
        double avRequestNs = clientStats.parallelStream().flatMap(o -> o.getRequestProcTimes().stream())
                .mapToLong(x -> x).average().orElse(0);
        double avSortingNs = clientStats.parallelStream().flatMap(o -> o.getSortingTimes().stream())
                .mapToLong(x -> x).average().orElse(0);
        return new ServerStats(avRequestNs, avSortingNs);
    }

    private final double avgRequestProcNs;
    private final double avgSortingNs;
}
