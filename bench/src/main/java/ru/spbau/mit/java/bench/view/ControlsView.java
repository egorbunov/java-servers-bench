package ru.spbau.mit.java.bench.view;


import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.converter.NumberStringConverter;
import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.bench.*;
import ru.spbau.mit.java.bench.stat.BenchmarkResults;
import ru.spbau.mit.java.client.runner.RunnerOpts;
import ru.spbau.mit.java.commons.ServArchitecture;

import java.util.Arrays;
import java.util.EnumMap;

/**
 * Class represents view of page with bench controls
 */
@Slf4j
public class ControlsView implements BenchmarkControllerListener {
    private ComboBox<ServArchitecture> archTypesComboBox;
    private final EnumMap<ru.spbau.mit.java.bench.Control, Slider> paramSliders = new EnumMap<>(ru.spbau.mit.java.bench.Control.class);
    private final EnumMap<ru.spbau.mit.java.bench.Control, TextField> paramTextFields = new EnumMap<>(ru.spbau.mit.java.bench.Control.class);
    private ComboBox<ru.spbau.mit.java.bench.Control> whatToChangeCB;
    private Slider changeFrom;
    private Slider changeTo;
    private Slider changeStep;
    private TextField serverRunnerHost;
    private TextField serverRunnerPort;
    private Button benchButton;
    private ProgressBar progressBar;
    private Slider repeatStepCnt;

    private final GridPane view;
    private final BenchmarkController benchmarkController;

    public ControlsView(BenchmarkController benchmarkController) {
        this.benchmarkController = benchmarkController;

        GridBuilder controlsGridBuilder = new GridBuilder(3)
                .vGap(10).hGap(15).padding(new Insets(15, 15, 15, 15));
        addArchitectureTypeControls(controlsGridBuilder);
        controlsGridBuilder.row().hSep();
        addVariableControls(controlsGridBuilder);
        controlsGridBuilder.row().hSep();
        addRangeParameterControls(controlsGridBuilder);
        controlsGridBuilder.row().hSep();
        addBenchmarkServerControls(controlsGridBuilder);
        controlsGridBuilder.row().hSep();
        addRepeatCntRow(controlsGridBuilder);
        controlsGridBuilder.row().hSep();
        addBenchButton(controlsGridBuilder);

        view = controlsGridBuilder.build();
    }

    private void addRepeatCntRow(GridBuilder builder) {
        repeatStepCnt = new Slider(1, 1000, 1);
        TextField tf = createEditableIntField(repeatStepCnt);
        builder.row().col(new Label("Each step repeat cnt: "))
                .col(repeatStepCnt).col(tf);
    }

    public GridPane getView() {
        return view;
    }

    private void addArchitectureTypeControls(GridBuilder builder) {
        archTypesComboBox = new ComboBox<>(
                FXCollections.observableList(Arrays.asList(ServArchitecture.values()))
        );
        builder.row().col(new Label("Server architecture: ")).col(archTypesComboBox);
        archTypesComboBox.setValue(ServArchitecture.TCP_THREAD_PER_CLIENT);
    }

    private void setupSliderForControl(ru.spbau.mit.java.bench.Control c, Slider slider) {
        slider.setBlockIncrement(1);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit((c.getMax() - c.getMin()) / 2);
        slider.setMin(c.getMin());
        slider.setMax(c.getMax());
    }

    private void addVariableControls(GridBuilder builder) {
        for (ru.spbau.mit.java.bench.Control c : ru.spbau.mit.java.bench.Control.values()) {
            Slider slider = new Slider(c.getMin(), c.getMax(), c.getMin());
            setupSliderForControl(c, slider);
            TextField tf = createEditableIntField(slider);
            paramTextFields.put(c, tf);
            GridPane.setMargin(tf, new Insets(0, 0, 0, 15));
            builder.row().col(new Label(c.toString())).col(slider).col(tf);
            paramSliders.put(c, slider);
        }
    }

    private void addBenchmarkServerControls(GridBuilder builder) {
        serverRunnerHost = new TextField("localhost");
        serverRunnerPort = new TextField("6666");
        builder.row().col(new Label("Benchmark server hostname: ")).col(serverRunnerHost)
                .row().col(new Label("Benchmark server port: ")).col(serverRunnerPort);
    }

    private void addBenchButton(GridBuilder builder) {
        benchButton = new Button();
        benchButton.setText("Do benchmark!");
        benchButton.setOnAction(event -> {
            log.info("Starting benchmark!");

            int port = 0;
            try {
                port = Integer.parseInt(serverRunnerPort.getText());
            } catch (NumberFormatException e) {
                Commons.showError("Port must be integer!");
                log.info("Bad port specified..");
                return;
            }

            BenchmarkSettings bs = new BenchmarkSettings(
                    new RunnerOpts(
                            (int) paramSliders.get(ru.spbau.mit.java.bench.Control.CLIENT_NUM).getValue(),
                            (int) paramSliders.get(ru.spbau.mit.java.bench.Control.ARRAY_LEN).getValue(),
                            (int) paramSliders.get(ru.spbau.mit.java.bench.Control.DELAY).getValue(),
                            (int) paramSliders.get(ru.spbau.mit.java.bench.Control.REQUSET_NUM).getValue()
                    ),
                    serverRunnerHost.getText(),
                    port,
                    archTypesComboBox.getValue(),
                    whatToChangeCB.getValue(),
                    (int) changeFrom.getValue(),
                    (int) changeTo.getValue(),
                    (int) changeStep.getValue(),
                    (int) repeatStepCnt.getValue()
            );
            log.info("Settings: " + bs.toString());
            benchmarkController.setSettings(bs);
            benchmarkController.startBenchmark();
        });

        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        GridPane.setConstraints(progressBar, 1, 12, 3, 2);
        builder.row().col(benchButton).col(progressBar, 2, 1);
    }

    private void addRangeParameterControls(GridBuilder builder) {
        whatToChangeCB = new ComboBox<>(FXCollections.observableList(Arrays.asList(ru.spbau.mit.java.bench.Control.values())));
        changeFrom = new Slider(0, 0, 0);
        changeTo = new Slider(0, 0, 0);
        changeStep = new Slider(1, 10000, 10);
        changeStep.setBlockIncrement(1);
        changeStep.setMajorTickUnit(10000 / 2 - 1);
        changeFrom.setDisable(true);
        changeTo.setDisable(true);
        changeStep.setDisable(true);

        whatToChangeCB.valueProperty().addListener((observable, oldValue, newValue) -> {
            changeFrom.setDisable(false);
            changeTo.setDisable(false);
            changeStep.setDisable(false);

            ru.spbau.mit.java.bench.Control c = observable.getValue();
            Slider sl = paramSliders.get(c);
            double len = sl.getMax() - sl.getMin();
            changeStep.setMin((int) (len / 10000));
            changeStep.setMax((int) (len / 5));
            changeStep.setBlockIncrement(1);
            changeStep.setShowTickLabels(true);
            changeStep.setMajorTickUnit((changeStep.getMax() - changeStep.getMin()) / 2);

            for (Slider s : paramSliders.values()) {
                s.setDisable(false);
            }
            for (TextField s : paramTextFields.values()) {
                s.setDisable(false);
            }
            setupSliderForControl(c, changeFrom);
            setupSliderForControl(c, changeTo);
            sl.setDisable(true);
            paramTextFields.get(c).setDisable(true);
        });

        changeTo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < changeFrom.getValue()) {
                changeTo.setValue(oldValue.doubleValue());
            }
        });
        changeFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > changeTo.getValue()) {
                changeFrom.setValue(oldValue.doubleValue());
            }
        });

        TextField changeToField = createEditableIntField(changeTo);
        TextField changeFromField = createEditableIntField(changeFrom);
        TextField changeStepField = createEditableIntField(changeStep);
        GridPane.setMargin(changeToField, new Insets(0, 0, 0, 15));
        GridPane.setMargin(changeFromField, new Insets(0, 0, 0, 15));
        GridPane.setMargin(changeStepField, new Insets(0, 0, 0, 15));

        whatToChangeCB.setValue(ru.spbau.mit.java.bench.Control.ARRAY_LEN);
        builder.row().col(new Label("What to change: ")).col(whatToChangeCB)
                .row().col(new Label("From: ")).col(changeFrom).col(changeFromField)
                .row().col(new Label("To: ")).col(changeTo).col(changeToField)
                .row().col(new Label("Step: ")).col(changeStep).col(changeStepField);
    }

    private TextField createEditableIntField(Slider sl) {
        TextField tf = new TextField();
        NumberStringConverter numConv = new NumberStringConverter();
        tf.setTextFormatter(new TextFormatter<>(numConv));

        EventHandler<ActionEvent> eventHandler = event -> {
            Number newNum = numConv.fromString(tf.getText());
            newNum = newNum == null ? sl.getMin() - 1 : newNum;
            if (newNum.intValue() > sl.getMax()) {
                tf.setText(numConv.toString(sl.getMax()));
            } else if (newNum.intValue() < sl.getMin()) {
                tf.setText(numConv.toString(sl.getMin()));
            } else {
                sl.setValue(newNum.intValue());
            }
        };

        tf.setOnAction(eventHandler);
        tf.focusedProperty().addListener((observable, oldValue, newValue) ->
                eventHandler.handle(null));

        tf.setText(numConv.toString(sl.getValue()));
        sl.valueProperty().addListener((observable, oldValue, newValue) ->
                tf.setText(numConv.toString(newValue.intValue())));
        return tf;
    }

    private void addSliderLabelListener(Slider sl, Label lb) {
        sl.valueProperty().addListener((observable, oldValue, newValue) ->
                lb.setText(Integer.toString(newValue.intValue())));
    }

    @Override
    public void onBenchmarkStarted(BenchmarkSettings settings) {
        benchButton.setDisable(true);
        progressBar.setVisible(true);
    }

    @Override
    public void onBenchmarkFinished(BenchmarkResults results) {
        benchButton.setDisable(false);
        progressBar.setVisible(false);
    }

    @Override
    public void onBenchmarkProgressUpdate(int progress, int goal) {
        progressBar.setProgress((double) progress / (double) goal);
    }

    @Override
    public void onBenchmarkError(String s) {
        Commons.showError(s);
        progressBar.setVisible(false);
        benchButton.setDisable(false);
    }
}
