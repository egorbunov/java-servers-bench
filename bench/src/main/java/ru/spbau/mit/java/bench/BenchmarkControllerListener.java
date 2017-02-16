package ru.spbau.mit.java.bench;

import ru.spbau.mit.java.bench.stat.BenchmarkResults;

public interface BenchmarkControllerListener {
    void onBenchmarkStarted(BenchmarkSettings settings);
    void onBenchmarkFinished(BenchmarkResults results);
    void onBenchmarkProgressUpdate(int progress, int goal);
    void onBenchmarkError(String s);
    void onClearResults();
}
