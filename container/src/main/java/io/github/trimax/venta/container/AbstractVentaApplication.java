package io.github.trimax.venta.container;

public interface AbstractVentaApplication<T> {
    void start(String[] args, T argument);
}
