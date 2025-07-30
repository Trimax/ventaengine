package com.venta.container;

public interface AbstractVentaApplication<T> {
    void start(String[] args, T argument);
}
