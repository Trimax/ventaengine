package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.entity.implementation.FontEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class TextState extends AbstractState {
    private final ProgramEntityImplementation program;
    private final FontEntityImplementation font;
    private final Geometry geometry;
}
