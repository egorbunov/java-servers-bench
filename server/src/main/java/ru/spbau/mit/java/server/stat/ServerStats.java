package ru.spbau.mit.java.server.stat;

import lombok.Data;

/**
 * Statistics
 */
@Data
public class ServerStats {
    private final double avgRequestProcMs;
    private final double avgSortingMs;
}
