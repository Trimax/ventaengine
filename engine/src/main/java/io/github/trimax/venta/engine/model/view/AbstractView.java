package io.github.trimax.venta.engine.model.view;

public interface AbstractView {
    String getID();

    String getName();

    default String getPublicInformation() {
        return String.format("%s (%s)", getID(), getName());
    }
}
