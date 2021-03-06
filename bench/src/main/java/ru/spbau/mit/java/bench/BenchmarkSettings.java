package ru.spbau.mit.java.bench;

import lombok.Data;
import ru.spbau.mit.java.client.runner.RunnerOpts;
import ru.spbau.mit.java.commons.ServerArch;

@Data
public class BenchmarkSettings {
    private final RunnerOpts runnerOpts;
    private final String benchServerHost;
    private final int benchServerPort;
    private final ServerArch serverArch;
    private final Control whatToChage;
    private final int from;
    private final int to;
    private final int step;
    private final int stepRepeatCnt;
}
