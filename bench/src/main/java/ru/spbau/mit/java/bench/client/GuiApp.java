package ru.spbau.mit.java.bench.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ru.spbau.mit.java.bench.client.stat.BenchmarkResults;
import ru.spbau.mit.java.bench.client.stat.FinalStat;
import ru.spbau.mit.java.bench.client.view.BenchLineChartView;
import ru.spbau.mit.java.bench.client.view.BenchResultTableView;
import ru.spbau.mit.java.bench.client.view.ControlsView;

import java.util.stream.Collectors;

public class GuiApp extends Application implements BenchmarkControllerListener {
    public static void main(String[] args) {
        launch(args);
    }

    private Tab controlsTab;
    private Tab requestAvTimeTab;
    private Tab sortAvTimeTab;
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
        BenchLineChartView requestAvTimePlot = new BenchLineChartView(
                primaryStage,
                results -> results.getData().stream().map(FinalStat::getAvRequestNs)
                        .collect(Collectors.toList()),
                "av. request time (ms)",
                "Average request time"
        );

        BenchLineChartView sortingTimePlot = new BenchLineChartView(
                primaryStage,
                results -> results.getData().stream().map(FinalStat::getAvSortNs)
                        .collect(Collectors.toList()),
                "av. sorting time (ms)",
                "Average sorting time"
        );

        BenchLineChartView clientLifetimePlot = new BenchLineChartView(
                primaryStage,
                results -> results.getData().stream().map(FinalStat::getAvClientLifetimeNs)
                        .collect(Collectors.toList()),
                "av. client lifespan (ms)",
                "Average client lifespan"
        );
        BenchResultTableView tableView = new BenchResultTableView(primaryStage);

        bc.addListener(controlsView);
        bc.addListener(requestAvTimePlot);
        bc.addListener(sortingTimePlot);
        bc.addListener(clientLifetimePlot);
        bc.addListener(this);
        bc.addListener(tableView);

        controlsTab = new Tab("Control", controlsView.getView());
        requestAvTimeTab = new Tab("Request time plot", requestAvTimePlot.getView());
        sortAvTimeTab = new Tab("Sort time plot", sortingTimePlot.getView());
        avClientLifePlotTab = new Tab("Client lifespan plot", clientLifetimePlot.getView());
        resultsTableTab = new Tab("Results table", tableView.getView());


        TabPane root = new TabPane(controlsTab, resultsTableTab,
                requestAvTimeTab, sortAvTimeTab, avClientLifePlotTab);
        requestAvTimeTab.setDisable(true);
        requestAvTimeTab.setClosable(false);
        sortAvTimeTab.setDisable(true);
        sortAvTimeTab.setClosable(false);
        avClientLifePlotTab.setDisable(true);
        avClientLifePlotTab.setClosable(false);
        resultsTableTab.setDisable(true);
        resultsTableTab.setClosable(false);
        primaryStage.setScene(new Scene(root, 650, 600));
        primaryStage.show();
    }

    @Override
    public void onBenchmarkStarted(BenchmarkSettings settings) {
        requestAvTimeTab.setDisable(true);
        sortAvTimeTab.setDisable(true);
        avClientLifePlotTab.setDisable(true);
        resultsTableTab.setDisable(true);

    }

    @Override
    public void onBenchmarkFinished(BenchmarkResults results) {
        requestAvTimeTab.setDisable(false);
        sortAvTimeTab.setDisable(false);
        avClientLifePlotTab.setDisable(false);
        resultsTableTab.setDisable(false);
    }

    @Override
    public void onBenchmarkProgressUpdate(int progress, int goal) {}

    @Override
    public void onBenchmarkError(String s) {
        requestAvTimeTab.setDisable(true);
        sortAvTimeTab.setDisable(true);
        avClientLifePlotTab.setDisable(true);
        resultsTableTab.setDisable(true);
    }
}
