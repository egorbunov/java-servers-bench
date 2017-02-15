package ru.spbau.mit.java.bench;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ru.spbau.mit.java.bench.stat.BenchmarkResults;
import ru.spbau.mit.java.bench.view.BenchmarkResultChartView;
import ru.spbau.mit.java.bench.view.BenchmarkResultTableView;
import ru.spbau.mit.java.bench.view.ControlsView;

import java.util.stream.Collectors;

public class GuiApp extends Application implements BenchmarkControllerListener {
    public static void main(String[] args) {
        launch(args);
    }

    private Tab controlsTab;
    private Tab sendReceiveGapTab;
    private Tab requestProcAvTimeTab;
    private Tab avClientLifePlotTab;
    private Tab resultsTableTab;


    @Override
    public void start(Stage primaryStage) {
        setup(primaryStage);
    }

    private void setup(Stage primaryStage) {
        primaryStage.setTitle("Server benchmark gui");

        BenchmarkController bc = new BenchmarkController();
        ControlsView controlsView = new ControlsView(bc);
        BenchmarkResultChartView sendReceiveGapPlot = new BenchmarkResultChartView(
                primaryStage,
                results -> results.getData().stream().map(x -> x.getAvReceiveSendGapNs() / 1e6)
                        .collect(Collectors.toList()),
                "av. time btw. receive and send (ms)",
                "Average receive-send time"
        );

        BenchmarkResultChartView requestProcTimePlot = new BenchmarkResultChartView(
                primaryStage,
                results -> results.getData().stream().map(x -> x.getAvReceiveSendGapNs() / 1e6)
                        .collect(Collectors.toList()),
                "av. request processing time (ms)",
                "Average processing time"
        );

        BenchmarkResultChartView clientLifetimePlot = new BenchmarkResultChartView(
                primaryStage,
                results -> results.getData().stream().map(x -> x.getAvClientLifetimeMs())
                        .collect(Collectors.toList()),
                "av. client lifespan (ms)",
                "Average client lifespan"
        );
        BenchmarkResultTableView tableView = new BenchmarkResultTableView(primaryStage);

        bc.addListener(controlsView);
        bc.addListener(sendReceiveGapPlot);
        bc.addListener(requestProcTimePlot);
        bc.addListener(clientLifetimePlot);
        bc.addListener(this);
        bc.addListener(tableView);

        controlsTab = new Tab("Control", controlsView.getView());
        sendReceiveGapTab = new Tab("Receive req. send resp. gap plot", sendReceiveGapPlot.getView());
        requestProcAvTimeTab = new Tab("Request proc. time plot", requestProcTimePlot.getView());
        avClientLifePlotTab = new Tab("Client lifespan plot", clientLifetimePlot.getView());
        resultsTableTab = new Tab("Results table", tableView.getView());


        TabPane root = new TabPane(controlsTab, resultsTableTab,
                sendReceiveGapTab, requestProcAvTimeTab, avClientLifePlotTab);
        sendReceiveGapTab.setDisable(true);
        sendReceiveGapTab.setClosable(false);
        requestProcAvTimeTab.setDisable(true);
        requestProcAvTimeTab.setClosable(false);
        avClientLifePlotTab.setDisable(true);
        avClientLifePlotTab.setClosable(false);
        resultsTableTab.setDisable(true);
        resultsTableTab.setClosable(false);
        primaryStage.setScene(new Scene(root, 650, 650));
        primaryStage.show();
    }

    @Override
    public void onBenchmarkStarted(BenchmarkSettings settings) {
        sendReceiveGapTab.setDisable(true);
        requestProcAvTimeTab.setDisable(true);
        avClientLifePlotTab.setDisable(true);
        resultsTableTab.setDisable(true);
    }

    @Override
    public void onBenchmarkFinished(BenchmarkResults results) {
        sendReceiveGapTab.setDisable(false);
        requestProcAvTimeTab.setDisable(false);
        avClientLifePlotTab.setDisable(false);
        resultsTableTab.setDisable(false);
    }

    @Override
    public void onBenchmarkProgressUpdate(int progress, int goal) {}

    @Override
    public void onBenchmarkError(String s) {
        sendReceiveGapTab.setDisable(true);
        requestProcAvTimeTab.setDisable(true);
        avClientLifePlotTab.setDisable(true);
        resultsTableTab.setDisable(true);
    }
}
