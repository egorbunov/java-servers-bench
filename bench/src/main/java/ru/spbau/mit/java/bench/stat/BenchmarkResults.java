package ru.spbau.mit.java.bench.stat;


import lombok.Data;
import ru.spbau.mit.java.bench.Control;

import java.util.List;

@Data
public class BenchmarkResults {
    private final Control whatChanges;
    private final int from;
    private final int to;
    private final int step;
    private final List<FinalStat> data;
}
