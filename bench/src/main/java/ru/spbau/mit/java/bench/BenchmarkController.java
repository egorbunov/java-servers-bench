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
import java.util.stream.Collectors;

@Slf4j
public class BenchmarkController {
    private BenchmarkSettings settings;
    private final List<BenchmarkControllerListener> listeners = new ArrayList<>();

    public void setSettings(BenchmarkSettings settings) {
        this.settings = settings;
    }

    public void addListener(BenchmarkControllerListener listener) {
        listeners.add(listener);
    }

    public void startBenchmark() {
        Thread t = new Thread(new BenchmarkTask());
        t.start();
    }

    private class BenchmarkTask implements Runnable {

        @Override
        public void run() {
            Platform.runLater(() -> listeners.forEach(l -> l.onBenchmarkStarted(settings)));

            ArrayList<FinalStat> finalStats = new ArrayList<>();

            int goalIter = settings.getStepRepeatCnt() *
                    (settings.getTo() - settings.getFrom()) / settings.getStep();
            int curProgress = 0;

            // iterating over changing parameter
            for (int i = settings.getFrom(); i <= settings.getTo(); i += settings.getStep()) {
                // constructing options
                RunnerOpts runnerOpts = constructRunnableOpts(i);
                List<FinalStat> stepStats = new ArrayList<>();
                // repeating one parameter calculations for robust statistics
                for (int j = 0; j < settings.getStepRepeatCnt(); ++j) {
                    FinalStat x = runOnce(runnerOpts, curProgress++, goalIter);
                    if (x != null) {
                        stepStats.add(x);
                    }
                }

                // averaging statistics!
                Tuple3<Double, Double, Double> res = stepStats.parallelStream().collect(
                        Tuple.collectors(
                                Collectors.averagingDouble(FinalStat::getAvRequestNs),
                                Collectors.averagingDouble(FinalStat::getAvSortNs),
                                Collectors.averagingDouble(FinalStat::getAvClientLifetimeMs)
                        )
                );

                finalStats.add(new FinalStat(
                        res.v1(), res.v2(), res.v3()
                ));
            }

            BenchmarkResults res = new BenchmarkResults(settings.getWhatToChage(),
                    settings.getFrom(), settings.getTo(), settings.getStep(),
                    finalStats);

            Platform.runLater(() -> {
                for (BenchmarkControllerListener l : listeners) {
                    l.onBenchmarkFinished(res);
                }
            });
        }

        private FinalStat runOnce(RunnerOpts runnerOpts, int curProgress, int goal) {
            Platform.runLater(() -> {
                for (BenchmarkControllerListener l : listeners) {
                    l.onBenchmarkProgressUpdate(curProgress, goal);
                }
            });

            BenchmarkClient bc = new BenchmarkClient(
                    runnerOpts,
                    settings.getBenchServerHost(),
                    settings.getBenchServerPort(),
                    settings.getServArchitecture(),
                    s -> Platform.runLater(() -> listeners.forEach(l -> l.onBenchmarkError(s)))
            );

            FinalStat oneRunRes = null;
            try {
                oneRunRes = bc.run();
            } catch (BenchmarkError benchServerError) {
                Platform.runLater(() -> listeners.forEach(
                        l -> l.onBenchmarkError(benchServerError.getMessage()))
                );
            }
            if (oneRunRes == null) {
                log.error("Got null stats, probably error");
            } else {
                log.debug("Got stats: " + oneRunRes);
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
            case REQUSET_NUM:
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
