package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.texture.AbstractTextureExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class TextureExecutor extends AbstractCoreExecutor {
    private TextureExecutor(@NonNull final ControllerFactory factory, @NonNull final List<AbstractTextureExecutor> executors) {
        super(factory, "texture", "the set of commands to manage textures", executors);
    }
}
