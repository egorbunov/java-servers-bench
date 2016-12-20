package ru.spbau.mit.java.client.runner;


import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.client.BenchClient;
import ru.spbau.mit.java.client.ClientCreator;
import ru.spbau.mit.java.commons.StoppableRunnable;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * ClientRunner starts clients with given options, these clients make requests
 * to server
 */

@Slf4j
public class ClientRunner implements Runnable {
    private final RunnerOpts opts;
    private final ClientCreator clientCreator;
    private final ArraySupplier arraySupplier;
    private final ExecutorService clientsExecutor;

    /**
     * @param opts options to configure clients behaviour
     * @param clientCreator clients producer
     */
    public ClientRunner(RunnerOpts opts, ClientCreator clientCreator) {

        this.opts = opts;
        this.clientCreator = clientCreator;
        this.arraySupplier = new ArraySupplier(opts.getArrayLen());
        this.clientsExecutor = Executors.newCachedThreadPool();
    }

    public void run() {
        log.debug("Running clients, opts: " + opts);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < opts.getClientNumber(); ++i) {
            futures.add(clientsExecutor.submit(new ClientTask()));
        }

        log.debug("All " + opts.getClientNumber() + " clients submitted, now waiting...");

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
                log.error("interrupt during future.get()");
            } catch (ExecutionException e) {
                log.error("Client execution excpetion: " + e.getCause().getMessage());
            }
        }

        log.debug("Finish!");
    }

    private class ClientTask extends StoppableRunnable {
        @Override
        public void run() {
            try (BenchClient client = clientCreator.create()) {
                for (int i = 0; i < opts.getRequestNumber(); ++i) {
                    client.makeBlockingRequest(arraySupplier.get());
                    Thread.sleep(opts.getDeltaMs());
                }
            } catch (IOException e) {
                log.error("IO excpetion occured: " + e.getMessage());
                throw new IOError(e);
            } catch (InterruptedException e) {
                log.error("Interrupt during sleep: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
