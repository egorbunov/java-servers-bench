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

public class GuiApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private Tab controlsTab;
    private Tab requestAvTimeTab;
    private Tab sortAvTimeTab;

    // controls
    private ComboBox<String> archTypesComboBox;
    private Slider arrayElNumS;
    private Slider clientNumberS;
    private Slider requestNumberS;
    private Slider delaySlider;
    private ComboBox<Control> whatToChangeCB;
    private Slider changeFrom;
    private Slider changeTo;

    private enum Control {
        CLIENT_NUM("Clients number"),
        REQUSET_NUM("Request number"),
        DELAY("Response-request gap"),
        ARRAY_LEN("Array to sort length");

        private final String name;

        Control(String s) {
            name = s;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void addSliderLabelListener(Slider sl, Label lb) {
        sl.valueProperty().addListener((observable, oldValue, newValue) -> {
            lb.setText(Integer.toString(newValue.intValue()));
        });
    }

    @Override
    public void start(Stage primaryStage) {
        setupUI(primaryStage);
    }

    public void benchmarkBtnClick() {

    }

    private void setupUI(Stage primaryStage) {
        primaryStage.setTitle("Server benchmark gui");

        archTypesComboBox = new ComboBox<>(
                FXCollections.observableList(new ArrayList<>(ServArchitecture.nameToId.keySet()))
        );

        arrayElNumS = new Slider(1, 500000, 1);
        arrayElNumS.setBlockIncrement(1);
        arrayElNumS.setShowTickLabels(true);
        arrayElNumS.setMajorTickUnit(500000 / 2);
        Label arrElNumLbl = new Label(Integer.toString((int) arrayElNumS.getValue()));
        addSliderLabelListener(arrayElNumS, arrElNumLbl);

        clientNumberS = new Slider(1, 10000, 1);
        clientNumberS.setBlockIncrement(1);
        clientNumberS.setShowTickLabels(true);
        clientNumberS.setMajorTickUnit(10000 / 2);
        Label clientNumberLbl = new Label(Integer.toString((int) clientNumberS.getValue()));
        addSliderLabelListener(clientNumberS, clientNumberLbl);

        requestNumberS = new Slider(1, 100000, 1);
        requestNumberS.setBlockIncrement(1);
        requestNumberS.setShowTickLabels(true);
        requestNumberS.setMajorTickUnit(100000 / 2);
        Label reqNumLbl = new Label(Integer.toString((int) requestNumberS.getValue()));
        addSliderLabelListener(requestNumberS, reqNumLbl);


        delaySlider = new Slider(0, 100000, 10);
        delaySlider.setBlockIncrement(1);
        delaySlider.setShowTickLabels(true);
        delaySlider.setMajorTickUnit(100000 / 2);
        Label delLbl = new Label(Integer.toString((int) delaySlider.getValue()));
        addSliderLabelListener(delaySlider, delLbl);


        whatToChangeCB = new ComboBox<>(FXCollections.observableList(Arrays.asList(Control.values())));
        changeFrom = new Slider(0, 0, 0);
        changeTo = new Slider(0, 0, 0);
        changeFrom.setDisable(true);
        changeTo.setDisable(true);
        Label changeToLbl = new Label(Integer.toString((int) changeTo.getValue()));
        addSliderLabelListener(changeTo, changeToLbl);
        Label changeFromLbl = new Label(Integer.toString((int) changeFrom.getValue()));
        addSliderLabelListener(changeFrom, changeFromLbl);

        changeTo.setBlockIncrement(1);
        changeFrom.setBlockIncrement(1);
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

        whatToChangeCB.valueProperty().addListener((observable, oldValue, newValue) -> {
            changeFrom.setDisable(false);
            changeTo.setDisable(false);
            arrayElNumS.setDisable(false);
            clientNumberS.setDisable(false);
            requestNumberS.setDisable(false);
            delaySlider.setDisable(false);
            if (observable.getValue().equals(Control.ARRAY_LEN)) {
                changeFrom.setShowTickLabels(true);
                changeFrom.setMajorTickUnit(500000 / 2);
                changeFrom.setMin(1);
                changeFrom.setMax(500000);
                changeTo.setShowTickLabels(true);
                changeTo.setMajorTickUnit(500000 / 2);
                changeTo.setMin(1);
                changeTo.setMax(500000);
                arrayElNumS.setDisable(true);
            } else if (observable.getValue().equals(Control.CLIENT_NUM)) {
                changeFrom.setShowTickLabels(true);
                changeFrom.setMajorTickUnit(10000 / 2);
                changeFrom.setMin(1);
                changeFrom.setMax(10000);
                changeTo.setShowTickLabels(true);
                changeTo.setMajorTickUnit(10000 / 2);
                changeTo.setMin(1);
                changeTo.setMax(10000);
                clientNumberS.setDisable(true);
            } else if (observable.getValue().equals(Control.REQUSET_NUM)) {
                changeFrom.setShowTickLabels(true);
                changeFrom.setMajorTickUnit(100000 / 2);
                changeFrom.setMin(1);
                changeFrom.setMax(100000);
                changeTo.setShowTickLabels(true);
                changeTo.setMajorTickUnit(100000 / 2);
                changeTo.setMin(1);
                changeTo.setMax(100000);
                requestNumberS.setDisable(true);
            } else if (observable.getValue().equals(Control.DELAY)) {
                changeFrom.setShowTickLabels(true);
                changeFrom.setMajorTickUnit(100000 / 2);
                changeFrom.setMin(0);
                changeFrom.setMax(100000);
                changeTo.setShowTickLabels(true);
                changeTo.setMajorTickUnit(100000 / 2);
                changeTo.setMin(0);
                changeTo.setMax(100000);
                delaySlider.setDisable(true);
            } else {
                changeTo.setDisable(true);
                changeFrom.setDisable(true);
            }
        });

        Button benchBtn = new Button();
        benchBtn.setText("Do benchmark!");
        benchBtn.setOnAction(event -> benchmarkBtnClick());

        GridPane controlsGrid = new GridPane();
        controlsGrid.setVgap(10);
        controlsGrid.setHgap(15);
        controlsGrid.setPadding(new Insets(15, 15, 15, 15));
        controlsGrid.add(new Label("Server architecture:"), 0, 0);
        controlsGrid.add(archTypesComboBox, 1, 0);
        controlsGrid.add(new Label(Control.CLIENT_NUM.name), 0, 1);

        controlsGrid.add(clientNumberS, 1, 1);
        GridPane.setMargin(clientNumberLbl, new Insets(0, 0, 0, 15));
        controlsGrid.add(clientNumberLbl, 2, 1);

        controlsGrid.add(new Label(Control.REQUSET_NUM.name), 0, 2);
        controlsGrid.add(requestNumberS, 1, 2);
        GridPane.setMargin(reqNumLbl, new Insets(0, 0, 0, 15));
        controlsGrid.add(reqNumLbl, 2, 2);

        controlsGrid.add(new Label(Control.DELAY.name), 0, 3);
        controlsGrid.add(delaySlider, 1, 3);
        GridPane.setMargin(delLbl, new Insets(0, 0, 0, 15));
        controlsGrid.add(delLbl, 2, 3);

        controlsGrid.add(new Label(Control.ARRAY_LEN.name), 0, 4);
        controlsGrid.add(arrayElNumS, 1, 4);
        GridPane.setMargin(arrElNumLbl, new Insets(0, 0, 0, 15));
        controlsGrid.add(arrElNumLbl, 2, 4);


        final Separator sep = new Separator();
        sep.setValignment(VPos.CENTER);
        GridPane.setConstraints(sep, 0, 5);
        GridPane.setColumnSpan(sep, 2);
        controlsGrid.getChildren().add(sep);

        controlsGrid.add(new Label("What to change: "), 0, 6);
        controlsGrid.add(whatToChangeCB, 1, 6);
        controlsGrid.add(new Label("From: "), 0, 7);
        controlsGrid.add(changeFrom, 1, 7);
        GridPane.setMargin(changeFromLbl, new Insets(0, 0, 0, 15));
        controlsGrid.add(changeFromLbl, 2, 7);
        controlsGrid.add(new Label("To:"), 0, 8);
        controlsGrid.add(changeTo, 1, 8);
        GridPane.setMargin(changeToLbl, new Insets(0, 0, 0, 15));
        controlsGrid.add(changeToLbl, 2, 8);


        GridPane.setConstraints(benchBtn, 0, 10);
        GridPane.setColumnSpan(benchBtn, 2);
        controlsGrid.getChildren().add(benchBtn);

        archTypesComboBox.setValue(ServArchitecture.idToName.get(0));
        whatToChangeCB.setValue(Control.ARRAY_LEN);

        controlsTab = new Tab("Control", controlsGrid);
        requestAvTimeTab = new Tab("Request time graph");
        sortAvTimeTab = new Tab("Sort time graph");

        TabPane root = new TabPane(controlsTab, requestAvTimeTab, sortAvTimeTab);
        requestAvTimeTab.setDisable(true);
        sortAvTimeTab.setDisable(true);
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();
    }
}
