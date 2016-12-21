package ru.spbau.mit.java.bench.client.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.bench.client.BenchmarkControllerListener;
import ru.spbau.mit.java.bench.client.BenchmarkSettings;
import ru.spbau.mit.java.bench.client.stat.BenchmarkResults;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Created by: Egor Gorbunov
 * Date: 12/21/16
 * Email: egor-mailbox@ya.com
 */
@Slf4j
public class BenchLineChartView implements BenchmarkControllerListener {
    private final Function<BenchmarkResults, List<Double>> resultMapper;
    private BenchmarkSettings bSettings;
    private final LineChart<Number, Number> lineChart;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    public BenchLineChartView(
            Stage parent,
            Function<BenchmarkResults, List<Double>> resultMapper,
            String yLabel,
            String title) {
        this.resultMapper = resultMapper;
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);


        // image saving
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Input png file name");
        final MenuItem saveItem = new MenuItem("Save as image");
        saveItem.setOnAction(event -> {
            WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);
            File file = fileChooser.showSaveDialog(parent);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                Commons.showError("Can't save image: " + e.getMessage());
                log.error("Can't save image: " + e.getMessage());
            }
        });
        final ContextMenu menu = new ContextMenu(
                saveItem
        );
        lineChart.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())) {
                menu.show(parent, event.getScreenX(), event.getScreenY());
            }
        });

    }

    public LineChart<Number, Number> getView() {
        return lineChart;
    }


    @Override
    public void onBenchmarkStarted(BenchmarkSettings settings) {
        bSettings = settings;
        xAxis.setLabel(settings.getWhatToChage().toString());
    }

    @Override
    public void onBenchmarkFinished(BenchmarkResults results) {
        List<Double> numbers = resultMapper.apply(results);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < numbers.size(); ++i) {
            double y = numbers.get(i) / 1e6; // to ms
            int x = results.getFrom() + results.getStep() * i;
            series.getData().add(new XYChart.Data<>(x, y));
            series.setName(bSettings.getServArchitecture().toString());
        }

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    @Override
    public void onBenchmarkProgressUpdate(int progress, int goal) {}

    @Override
    public void onBenchmarkError(String s) {}
}
