package io.github.trimax.venta.engine.model.common.geo;

public record Buffer(int id,
                     int count,
                     int length) {
    public boolean isValid() {
        return id > 0;
    }
}
