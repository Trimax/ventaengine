package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.model.instance.FontInstance;
import io.github.trimax.venta.engine.model.instance.ProgramInstance;
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
