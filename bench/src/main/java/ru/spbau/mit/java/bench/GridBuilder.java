package ru.spbau.mit.java.bench;


import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;

public class GridBuilder {
    private final GridPane grid = new GridPane();
    private int curRow = 0;
    private int curCol = 0;
    private final int maxCol;

    public GridBuilder(int maxCol) {
        this.maxCol = maxCol;
    }

    public GridBuilder vGap(int val) {
        grid.setVgap(val);
        return this;
    }

    public GridBuilder hGap(int val) {
        grid.setHgap(val);
        return this;
    }

    public GridBuilder padding(Insets insets) {
        grid.setPadding(insets);
        return this;
    }

    public GridBuilder hSep() {
        final Separator sep = new Separator();
        sep.setValignment(VPos.CENTER);
        GridPane.setConstraints(sep, 0, curRow);
        GridPane.setColumnSpan(sep, maxCol);
        grid.getChildren().add(sep);
        return this;
    }

    public GridBuilder row() {
        curRow += 1;
        curCol = 0;
        return this;
    }

    public GridBuilder col(Node node) {
        grid.add(node, curCol++, curRow);
        return this;
    }

    public GridBuilder col(Node node, int colspan, int rowspan) {
        grid.add(node, curCol, curRow, colspan, rowspan);
        curCol += colspan;
        curRow += rowspan;
        return this;
    }

    public GridPane build() {
        return grid;
    }
}
