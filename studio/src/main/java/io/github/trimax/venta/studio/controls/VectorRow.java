package io.github.trimax.venta.studio.controls;

import java.util.function.BiConsumer;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * A row of fields for the numbers of a vector, each with the name of its axis: a drag of the name over the row changes the number, as the
 * sliders of the editors of games do.
 */
public final class VectorRow extends HBox {
    private static final String[] AXES = {"X", "Y", "Z", "W"};

    /** The number of the pixels of a drag that is a step of the field */
    private static final float PIXELS_PER_STEP = 10.f;

    private final NumberField[] fields;

    /**
     * A row of fields.
     *
     * @param title    what the row is called
     * @param count    how many numbers the vector has
     * @param step     the step of an arrow
     * @param onChange gets the axis and the new value of it
     */
    public VectorRow(final String title, final int count, final float step, final BiConsumer<Integer, Float> onChange) {
        super(6);
        setPadding(new Insets(2, 0, 2, 0));

        final var name = new Label(title);
        name.setMinWidth(62);
        name.getStyleClass().add("dim");
        getChildren().add(name);

        fields = new NumberField[count];
        for (int axis = 0; axis < count; axis++) {
            final var index = axis;
            final var field = new NumberField(step, value -> onChange.accept(index, value));
            fields[axis] = field;

            final var label = new Label(AXES[axis]);
            label.getStyleClass().add("axis-" + AXES[axis].toLowerCase());
            label.setStyle("-fx-cursor: h-resize;");
            label.setOnMouseDragged(event -> scrub(field, event));
            label.setOnMousePressed(event -> label.setUserData(event.getSceneX()));

            final var cell = new HBox(3, label, field);
            HBox.setHgrow(cell, Priority.ALWAYS);
            HBox.setHgrow(field, Priority.ALWAYS);
            field.setMinWidth(40);
            getChildren().add(cell);
        }
    }

    /** Shows the numbers of the vector, the row doesn't report them */
    public void setValues(final float... values) {
        for (int axis = 0; axis < fields.length && axis < values.length; axis++)
            fields[axis].setValue(values[axis]);
    }

    private void scrub(final NumberField field, final MouseEvent event) {
        final var source = (Label) event.getSource();
        final var previous = source.getUserData() instanceof Double x ? x : event.getSceneX();

        final var pixels = (float) (event.getSceneX() - previous);
        source.setUserData(event.getSceneX());

        if (pixels != 0.f)
            field.change(pixels * field.getStep() / PIXELS_PER_STEP * (event.isShiftDown() ? 10.f : 1.f));
    }
}
