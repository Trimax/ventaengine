package io.github.trimax.venta.mocks.set8;

import io.github.trimax.venta.container.annotations.Component;

@Component
public final class ResourceComponent implements AutoCloseable {
    @Override
    public void close() {
        Events.CLOSED.add(getClass().getSimpleName());
    }
}
