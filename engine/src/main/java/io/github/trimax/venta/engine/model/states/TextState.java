package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.model.entity.FontInstance;
import io.github.trimax.venta.engine.model.entity.ProgramInstance;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class TextState extends AbstractState {
    private final ProgramInstance program;
    private final FontInstance font;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;
}
