package io.github.trimax.venta.mocks.set8;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/** Depends on the resource, so it must be closed before it */
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserComponent implements AutoCloseable {
    private final ResourceComponent resource;

    @Override
    public void close() {
        Events.CLOSED.add(getClass().getSimpleName());
    }
}
