package ru.spbau.mit.java.bench.view;


import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.bench.BenchmarkControllerListener;
import ru.spbau.mit.java.bench.BenchmarkSettings;
import ru.spbau.mit.java.bench.stat.BenchmarkResults;
import ru.spbau.mit.java.bench.stat.FinalStat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BenchmarkResultTableView implements BenchmarkControllerListener {
    private final TableView<Row> table = new TableView<>();
    private final VBox tableBox;
    private BenchmarkSettings curSettings;
    private final Label tableLabel;

    @Data
    public static class Row {
        private final int param;
        private final double avSortTime;
        private final double avRequestTime;
        private final double avClientLifespan;
    }

    public VBox getView() {
        return tableBox;
    }

    public BenchmarkResultTableView(Stage parent) {
        tableLabel = new Label("Benchmark data");
        tableLabel.setFont(new Font("Arial", 15));
        table.setEditable(false);
        tableBox = new VBox();
        tableBox.setSpacing(5);
        tableBox.setPadding(new Insets(10, 10, 0, 10));
        tableBox.getChildren().addAll(tableLabel, table);

        // csv saving
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Input txt file name");
        final MenuItem saveItem = new MenuItem("Save as txt file");
        saveItem.setOnAction(event -> {
            File file = fileChooser.showSaveDialog(parent);
            if (file == null) {
                return;
            }
            List<String> columns =
                    table.getColumns().stream().map(TableColumn::getText).collect(Collectors.toList());

            try (FileWriter writer = new FileWriter(file)) {
                for (int i = 0; i < columns.size(); ++i) {
                    writer.write(columns.get(i));
                    if (i != columns.size() - 1) {
                        writer.write(",");
                    }
                }
                writer.write(System.lineSeparator());
                for (Row r : table.getItems()) {
                    writer.write(Integer.toString(r.param));
                    writer.write(',');
                    writer.write(Double.toString(r.avSortTime));
                    writer.write(',');
                    writer.write(Double.toString(r.avRequestTime));
                    writer.write(',');
                    writer.write(Double.toString(r.avClientLifespan));
                    writer.write(System.lineSeparator());
                }
            } catch (IOException e) {
                log.error("Can't save file");
            }
        });
        final ContextMenu menu = new ContextMenu(
                saveItem
        );
        table.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())) {
                menu.show(parent, event.getScreenX(), event.getScreenY());
            }
        });
    }

    @Override
    public void onBenchmarkStarted(BenchmarkSettings settings) {
        this.curSettings = settings;
        tableLabel.setText("Benchmark data. "
                + settings.getServArchitecture() + "; "
                + settings.getRunnerOpts());
    }

    @Override
    public void onBenchmarkFinished(BenchmarkResults results) {
        table.getColumns().clear();

        TableColumn<Row, Number> paramColumn =
                new TableColumn<>(curSettings.getWhatToChage().toString());
        paramColumn.setCellValueFactory(
                new PropertyValueFactory<>("param")
        );
        TableColumn<Row, Number> avSortTimeCol =
                new TableColumn<>("Sort time");
        avSortTimeCol.setCellValueFactory(
                new PropertyValueFactory<>("avSortTime")
        );
        TableColumn<Row, Number> avRequestProcTime =
                new TableColumn<>("Request proc. time");
        avRequestProcTime.setCellValueFactory(
                new PropertyValueFactory<>("avRequestTime")
        );
        TableColumn<Row, Number> avClientLifespan =
                new TableColumn<>("Client lifespan");
        avClientLifespan.setCellValueFactory(
                new PropertyValueFactory<>("avClientLifespan")
        );
        List<FinalStat> data = results.getData();
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < data.size(); ++i) {
            FinalStat x = data.get(i);
            rows.add(new Row(curSettings.getFrom() + i * curSettings.getStep(),
                    x.getAvSortNs() / 1e6,
                    x.getAvRequestNs() / 1e6,
                    x.getAvClientLifetimeMs() / 1e6));
        }
        table.setItems(FXCollections.observableArrayList(rows));
        table.getColumns().addAll(paramColumn, avSortTimeCol, avRequestProcTime, avClientLifespan);
    }

    @Override
    public void onBenchmarkProgressUpdate(int progress, int goal) {}

    @Override
    public void onBenchmarkError(String s) {}
}
