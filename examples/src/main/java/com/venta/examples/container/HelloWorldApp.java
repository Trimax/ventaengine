package com.venta.examples.container;

import com.venta.container.AbstractVentaApplication;
import com.venta.container.annotations.Component;

@Component
public class HelloWorldApp implements AbstractVentaApplication<Void> {
    @Override
    public void start(final String[] args, final Void argument) {
        System.out.println("Hello world");
    }
}