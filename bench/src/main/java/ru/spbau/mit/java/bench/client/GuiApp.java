package ru.spbau.mit.java.bench.client;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.spbau.mit.java.commons.ServArchitecture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

public class GuiApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private Tab controlsTab;
    private Tab requestAvTimeTab;
    private Tab sortAvTimeTab;

    // controls
    private ComboBox<ServArchitecture> archTypesComboBox;

    private EnumMap<Control, Slider> paramSliders = new EnumMap<>(Control.class);

    private Slider arrayElNumS;
    private Slider clientNumberS;
    private Slider requestNumberS;
    private Slider delaySlider;
    private ComboBox<Control> whatToChangeCB;
    private Slider changeFrom;
    private Slider changeTo;
    private Slider changeStep;

    private TextField serverRunnerHost;
    private TextField serverRunnerPort;



    @Override
    public void start(Stage primaryStage) {
        setupUI(primaryStage);
    }

    public void benchmarkBtnClick() {
        System.out.println("BENCH!!!");
    }

    private void setupUI(Stage primaryStage) {
        primaryStage.setTitle("Server benchmark gui");

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
        addBenchButton(controlsGridBuilder);

        controlsTab = new Tab("Control", controlsGridBuilder.build());
        requestAvTimeTab = new Tab("Request time graph");
        sortAvTimeTab = new Tab("Sort time graph");

        TabPane root = new TabPane(controlsTab, requestAvTimeTab, sortAvTimeTab);
        requestAvTimeTab.setDisable(true);
        sortAvTimeTab.setDisable(true);
        primaryStage.setScene(new Scene(root, 650, 600));
        primaryStage.show();
    }

    GridBuilder addArchitectureTypeControls(GridBuilder builder) {
        archTypesComboBox = new ComboBox<>(
                FXCollections.observableList(Arrays.asList(ServArchitecture.values()))
        );
        builder.row().col(new Label("Server architecture: ")).col(archTypesComboBox);
        archTypesComboBox.setValue(ServArchitecture.TCP_THREAD_PER_CLIENT);
        return builder;
    }

    void setupSliderForControl(Control c, Slider slider) {
        slider.setBlockIncrement(1);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit((c.getMax() + c.getMin()) / 2);
        slider.setMin(c.getMin());
        slider.setMax(c.getMax());
    }

    GridBuilder addVariableControls(GridBuilder builder) {
        for (Control c : Control.values()) {
            Slider slider = new Slider(c.getMin(), c.getMax(), c.getMin());
            setupSliderForControl(c, slider);
            Label label = new Label(Integer.toString((int) slider.getValue()));
            addSliderLabelListener(slider, label);
            GridPane.setMargin(label, new Insets(0, 0, 0, 15));
            builder.row().col(new Label(c.toString())).col(slider).col(label);
            paramSliders.put(c, slider);
        }
        return builder;
    }

    GridBuilder addBenchmarkServerControls(GridBuilder builder) {
        serverRunnerHost = new TextField("localhost");
        serverRunnerPort = new TextField("6666");
        return builder.row().col(new Label("Benchmark server hostname: ")).col(serverRunnerHost)
                .row().col(new Label("Benchmark server port: ")).col(serverRunnerPort);
    }

    GridBuilder addBenchButton(GridBuilder builder) {
        Button benchBtn = new Button();
        benchBtn.setText("Do benchmark!");
        benchBtn.setOnAction(event -> benchmarkBtnClick());
        return builder.row().col(benchBtn);
    }

    GridBuilder addRangeParameterControls(GridBuilder builder) {
        whatToChangeCB = new ComboBox<>(FXCollections.observableList(Arrays.asList(Control.values())));
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
            for (Slider s : paramSliders.values()) {
                s.setDisable(false);
            }
            Control c = observable.getValue();
            setupSliderForControl(c, changeFrom);
            setupSliderForControl(c, changeTo);
            paramSliders.get(c).setDisable(true);
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

        Label changeToLbl = new Label(Integer.toString((int) changeTo.getValue()));
        addSliderLabelListener(changeTo, changeToLbl);
        Label changeFromLbl = new Label(Integer.toString((int) changeFrom.getValue()));
        addSliderLabelListener(changeFrom, changeFromLbl);
        Label changeStepLbl = new Label(Integer.toString((int) changeStep.getValue()));
        addSliderLabelListener(changeStep, changeStepLbl);
        GridPane.setMargin(changeToLbl, new Insets(0, 0, 0, 15));
        GridPane.setMargin(changeFromLbl, new Insets(0, 0, 0, 15));
        GridPane.setMargin(changeStepLbl, new Insets(0, 0, 0, 15));

        whatToChangeCB.setValue(Control.ARRAY_LEN);
        builder.row().col(new Label("What to change: ")).col(whatToChangeCB)
                .row().col(new Label("From: ")).col(changeFrom).col(changeFromLbl)
                .row().col(new Label("To: ")).col(changeTo).col(changeToLbl)
                .row().col(new Label("Step: ")).col(changeStep).col(changeStepLbl);

        return builder;
    }

    private void addSliderLabelListener(Slider sl, Label lb) {
        sl.valueProperty().addListener((observable, oldValue, newValue) -> {
            lb.setText(Integer.toString(newValue.intValue()));
        });
    }
}
