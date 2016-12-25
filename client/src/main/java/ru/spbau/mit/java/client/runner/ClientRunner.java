package ru.spbau.mit.java.client.runner;


import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.client.Client;
import ru.spbau.mit.java.client.ClientCreator;
import ru.spbau.mit.java.client.stat.ClientStat;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * ClientRunner starts clients with given options, these clients make requests
 * to server
 */

@Slf4j
public class ClientRunner implements Callable<Double> {
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
        this.clientsExecutor = Executors.newFixedThreadPool(4);
    }

    /**
     * Returns average client lifetime (nano seconds)
     */
    public double run() {
        log.debug("Running clients, opts: " + opts);

        List<Future<ClientStat>> futures = new ArrayList<>();
        for (int i = 0; i < opts.getClientNumber(); ++i) {
            futures.add(clientsExecutor.submit(new ClientTask()));
        }

        log.debug("All " + opts.getClientNumber() + " clients submitted, now waiting...");

        ArrayList<ClientStat> stats = new ArrayList<>();
        for (Future<ClientStat> f : futures) {
            try {
                stats.add(f.get());
            } catch (InterruptedException e) {
                log.error("interrupt during future.get()");
            } catch (ExecutionException e) {
                log.error("Client execution exception: " + e.getCause().getMessage());
            }
        }
        clientsExecutor.shutdown();
        log.debug("Finish!");

        return stats.stream().mapToLong(ClientStat::getLifetimeMs).average().orElse(0);
    }

    @Override
    public Double call() throws Exception {
        return run();
    }

    private class ClientTask implements Callable<ClientStat> {
        @Override
        public ClientStat call() throws Exception {
            try (Client client = clientCreator.create()) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < opts.getRequestNumber(); ++i) {
                    client.makeBlockingRequest(arraySupplier.get());
                    Thread.sleep(opts.getDeltaMs());
                }
                long end = System.currentTimeMillis();
                return new ClientStat(end - start);
            } catch (IOException e) {
                log.error("IO exception ocurred: " + e.getMessage());
                throw new IOError(e);
            } catch (InterruptedException e) {
                log.error("Interrupt during sleep: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
