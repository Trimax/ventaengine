package io.github.trimax.venta.engine.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.TextVertexLayout;
import io.github.trimax.venta.engine.model.states.TextState;
import io.github.trimax.venta.engine.registries.implementation.FontRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextController extends AbstractController<TextState, Void> {
    private final ProgramRegistryImplementation programRegistry;
    private final FontRegistryImplementation fontRegistry;
    private final GeometryHelper geometryHelper;

    @Override
    protected TextState create(final Void argument) {
        log.debug("Initializing text");

        return new TextState(programRegistry.get(ProgramType.Text.getProgramName()),
                fontRegistry.get("DejaVuSansMono"),
                geometryHelper.create("Text", TextVertexLayout.class, new float[24], null, null));
    }

    @Override
    protected void destroy(@NonNull final TextState state) {
        log.debug("Deinitializing text");

        geometryHelper.delete(state.getGeometry());
    }
}
