package io.github.trimax.venta.studio.model.event;

/**
 * Something for the status bar: what has happened, or what the selection is.
 */
public record StatusEvent(String text) {
    public StatusEvent(final String format, final Object... arguments) {
        this(String.format(format, arguments));
    }
}
