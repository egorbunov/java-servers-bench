package ru.spbau.mit.java.bench;


import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import ru.spbau.mit.java.bench.client.BenchmarkError;
import ru.spbau.mit.java.bench.client.BenchmarkClient;
import ru.spbau.mit.java.bench.stat.BenchmarkResults;
import ru.spbau.mit.java.bench.stat.FinalStat;
import ru.spbau.mit.java.client.runner.RunnerOpts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class is responsible for running benchmark and notifying it's
 * listeners for benchmark results
 */
@Slf4j
public class BenchmarkController {
    private BenchmarkSettings settings;
    private final List<BenchmarkControllerListener> listeners = new ArrayList<>();
    private Thread benchmarkThread;
    private Consumer<String> statusListener = s -> {};

    public void setSettings(BenchmarkSettings settings) {
        this.settings = settings;
    }

    void addListener(BenchmarkControllerListener listener) {
        listeners.add(listener);
    }

    void setStatusListener(Consumer<String> statusListener) {
        this.statusListener = statusListener;
    }

    /**
     * Tells all listeners to clear results, if they can do it =)
     */
    public void clearResults() {
        listeners.forEach(BenchmarkControllerListener::onClearResults);
    }

    /**
     * Starts BenchmarkClient's in a separate thread.
     */
    public void startBenchmark() {
        benchmarkThread = new Thread(new BenchmarkTask());
        benchmarkThread.start();
    }

    /**
     * Tries to interrupt running benchmark
     */
    public void interruptBenchmark() {
        benchmarkThread.interrupt();
    }

    private class BenchmarkTask implements Runnable {

        @Override
        public void run() {
            Platform.runLater(() -> listeners.forEach(l -> l.onBenchmarkStarted(settings)));

            ArrayList<FinalStat> finalStats = new ArrayList<>();

            int oneRepeatStepCnt = (settings.getTo() - settings.getFrom()) / settings.getStep() + 1;
            int goalRepeats = settings.getStepRepeatCnt() * oneRepeatStepCnt;
            int curProgress = 0;


            for (int i = settings.getFrom(); i <= settings.getTo(); i += settings.getStep()) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                // constructing options
                RunnerOpts runnerOpts = constructRunnableOpts(i);
                List<FinalStat> stepStats = new ArrayList<>();
                // repeating one parameter calculations for robust statistics
                for (int j = 0; j < settings.getStepRepeatCnt(); ++j) {
                    FinalStat x;
                    try {
                        x = runOnce(runnerOpts, curProgress++, goalRepeats);
                    } catch (BenchmarkError e) {
                        Platform.runLater(() -> listeners.forEach(
                                l -> l.onBenchmarkError(e.getMessage()))
                        );
                        return;
                    }
                    if (x != null) {
                        stepStats.add(x);
                    }
                }
                // averaging statistics!
                Tuple3<Double, Double, Double> res = stepStats.parallelStream().collect(
                        Tuple.collectors(
                                Collectors.averagingDouble(FinalStat::getAvReceiveSendGapNs),
                                Collectors.averagingDouble(FinalStat::getAvRequestProcNs),
                                Collectors.averagingDouble(FinalStat::getAvClientLifetimeMs)
                        )
                );

                finalStats.add(new FinalStat(
                        res.v1(), res.v2(), res.v3()
                ));
            }

            BenchmarkResults res = new BenchmarkResults(
                    settings.getWhatToChage(),
                    settings.getFrom(),
                    settings.getTo(),
                    settings.getStep(),
                    finalStats);

            Platform.runLater(() -> {
                for (BenchmarkControllerListener l : listeners) {
                    l.onBenchmarkFinished(res);
                }
            });
        }

        private FinalStat runOnce(RunnerOpts runnerOpts, int curProgress, int goal) throws BenchmarkError {
            Platform.runLater(() -> {
                for (BenchmarkControllerListener l : listeners) {
                    l.onBenchmarkProgressUpdate(curProgress, goal);
                }
            });

            BenchmarkClient bc = new BenchmarkClient(
                    runnerOpts,
                    settings.getBenchServerHost(),
                    settings.getBenchServerPort(),
                    settings.getServerArch(),
                    s -> Platform.runLater(() -> listeners.forEach(l -> l.onBenchmarkError(s))),
                    s -> Platform.runLater(() -> statusListener.accept(s))
            );

            FinalStat oneRunRes = bc.run();
            if (oneRunRes == null) {
                throw new BenchmarkError("Got NULL stats! Error");
            }
            return oneRunRes;
        }
    }

    private @NotNull RunnerOpts constructRunnableOpts(int val) {
        switch (settings.getWhatToChage()) {
            case CLIENT_NUM:
                return new RunnerOpts(
                        val,
                        settings.getRunnerOpts().getArrayLen(),
                        settings.getRunnerOpts().getDeltaMs(),
                        settings.getRunnerOpts().getRequestNumber());
            case REQUEST_NUM:
                return new RunnerOpts(
                        settings.getRunnerOpts().getClientNumber(),
                        settings.getRunnerOpts().getArrayLen(),
                        settings.getRunnerOpts().getDeltaMs(),
                        val
                );
            case DELAY:
                return new RunnerOpts(
                        settings.getRunnerOpts().getClientNumber(),
                        settings.getRunnerOpts().getArrayLen(),
                        val,
                        settings.getRunnerOpts().getRequestNumber());
            case ARRAY_LEN:
                return new RunnerOpts(
                        settings.getRunnerOpts().getClientNumber(),
                        val,
                        settings.getRunnerOpts().getDeltaMs(),
                        settings.getRunnerOpts().getRequestNumber());
            default:
                throw new RuntimeException("What?");
        }
    }
}
