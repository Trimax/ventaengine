package io.github.trimax.venta.mocks.set8;

import java.util.List;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderTestApplication implements AbstractVentaApplication<Void> {
    private final List<Plugin> plugins;
    private final UserComponent user;

    @Override
    public void start(final String[] args, final Void argument) {
        plugins.forEach(plugin -> Events.PLUGINS.add(plugin.getClass().getSimpleName()));
    }
}
