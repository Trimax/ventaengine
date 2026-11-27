package io.github.trimax.venta.studio.controls;

import java.util.function.Consumer;

import io.github.trimax.venta.studio.utils.FormatUtil;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A field for a number: the value is taken when Enter is pressed or the field is left, a text that is not a number is taken back, and
 * the arrows change the value by a step.
 */
public final class NumberField extends TextField {
    private final float step;
    private final Consumer<Float> onChange;

    private float value;
    private boolean updating;

    /**
     * A field for a number.
     *
     * @param step     what an arrow adds, and what a drag of the label of the field adds for a pixel (a tenth of it)
     * @param onChange gets the value that the user has typed or has stepped to
     */
    public NumberField(final float step, final Consumer<Float> onChange) {
        this.step = step;
        this.onChange = onChange;

        setPrefColumnCount(5);
        setOnAction(event -> commit());
        focusedProperty().addListener((observable, was, is) -> {
            if (!is)
                commit();
        });
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                change(event.getCode() == KeyCode.UP ? step : -step);
                event.consume();
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                setText(FormatUtil.number(value));
                getParent().requestFocus();
            }
        });
    }

    public float getValue() {
        return value;
    }

    /** Shows a value that the field didn't produce: the change is not reported */
    public void setValue(final float value) {
        if (isFocused() && value == this.value)
            return;

        updating = true;
        try {
            this.value = value;
            setText(FormatUtil.number(value));
        } finally {
            updating = false;
        }
    }

    /** Adds to the value and reports it */
    public void change(final float delta) {
        accept(value + delta);
    }

    private void commit() {
        if (updating)
            return;

        try {
            final var typed = Float.parseFloat(getText().trim().replace(',', '.'));
            if (Float.isFinite(typed))
                accept(typed);
            else
                setText(FormatUtil.number(value));
        } catch (final NumberFormatException exception) {
            setText(FormatUtil.number(value));
        }
    }

    private void accept(final float next) {
        if (next == value && getText().equals(FormatUtil.number(next)))
            return;

        value = next;
        setText(FormatUtil.number(next));
        onChange.accept(next);
    }

    /** What an arrow adds */
    public float getStep() {
        return step;
    }
}
