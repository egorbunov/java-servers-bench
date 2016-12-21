package ru.spbau.mit.java.bench.client;

import ru.spbau.mit.java.bench.client.stat.BenchmarkResults;

public interface BenchmarkControllerListener {
    void onBenchmarkStarted(BenchmarkSettings settings);
    void onBenchmarkFinished(BenchmarkResults results);
    void onBenchmarkProgressUpdate(int progress, int goal);
    void onBenchmarkError(String s);
}
