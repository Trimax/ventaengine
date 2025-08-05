package io.github.trimax.venta.engine.model.instance;

public interface AbstractInstance {
    String getID();

    String getName();

    default String getPublicInformation() {
        return String.format("%s: %s", getID(), getName());
    }
}
