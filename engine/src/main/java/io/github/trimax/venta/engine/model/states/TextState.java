package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.model.instance.implementation.FontInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.ProgramInstanceImplementation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class TextState extends AbstractState {
    private final ProgramInstanceImplementation program;
    private final FontInstanceImplementation font;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;
}
