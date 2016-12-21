package ru.spbau.mit.java.bench;


import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.bench.client.BenchError;
import ru.spbau.mit.java.bench.client.BenchmarkClient;
import ru.spbau.mit.java.bench.stat.BenchmarkResults;
import ru.spbau.mit.java.bench.stat.FinalStat;
import ru.spbau.mit.java.client.runner.RunnerOpts;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BenchmarkController {
    private BenchmarkSettings settings;
    private List<BenchmarkControllerListener> listeners = new ArrayList<>();

    public void setSettings(BenchmarkSettings settings) {
        this.settings = settings;
    }

    public void addListener(BenchmarkControllerListener listener) {
        listeners.add(listener);
    }

    public void startBenchmark() {
        Thread t = new Thread(() -> {
            Platform.runLater(() -> listeners.forEach(l -> l.onBenchmarkStarted(settings)));

            ArrayList<FinalStat> finalStats = new ArrayList<>();
            for (int i = settings.getFrom(); i <= settings.getTo(); i += settings.getStep()) {
                int finalI = i;
                Platform.runLater(() -> {
                    for (BenchmarkControllerListener l : listeners) {
                        int progress = (finalI - settings.getFrom()) / settings.getStep();
                        int goal =  (settings.getTo() - settings.getFrom()) / settings.getStep();
                        l.onBenchmarkProgressUpdate(progress, goal);
                    }
                });


                RunnerOpts runnerOpts = constructRunnableOpts(i);
                if (runnerOpts == null) {
                    log.error("Null options!");
                    listeners.forEach(l -> l.onBenchmarkError("Error: got null options!"));
                    break;
                }
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
                } catch (BenchError benchServerError) {
                    Platform.runLater(() -> listeners.forEach(
                            l -> l.onBenchmarkError(benchServerError.getMessage()))
                    );
                }
                if (oneRunRes == null) {
                    log.error("Got null stats, probably error");
                    break;
                } else {
                    finalStats.add(oneRunRes);
                    log.debug("Got stats: " + finalStats.get(finalStats.size() - 1));
                }
            }
            BenchmarkResults res = new BenchmarkResults(settings.getWhatToChage(),
                    settings.getFrom(), settings.getTo(), settings.getStep(), finalStats);

            Platform.runLater(() -> {
                for (BenchmarkControllerListener l : listeners) {
                    l.onBenchmarkFinished(res);
                }
            });
        });
        t.start();
    }

    private RunnerOpts constructRunnableOpts(int val) {
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
        }
        return null;
    }


}
