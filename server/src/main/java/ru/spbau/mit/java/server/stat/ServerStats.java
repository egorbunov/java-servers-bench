package ru.spbau.mit.java.server.stat;

import lombok.Data;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Statistics
 */
@Data
public class ServerStats {
    public static ServerStats calc(Stream<OneRequestStats> statsStream) {
        Tuple2<Double, Double> finalStats = statsStream.collect(
                Tuple.collectors(
                        Collectors.averagingDouble(OneRequestStats::getRequestProcTime),
                        Collectors.averagingDouble(OneRequestStats::getSortingTime)
                )
        );
        return new ServerStats(finalStats.v1(), finalStats.v2());
    }

    private final double avgRequestProcNs;
    private final double avgSortingNs;
}
