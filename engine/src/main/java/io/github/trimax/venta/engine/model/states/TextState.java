package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.model.entity.FontEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class TextState extends AbstractState {
    private final ProgramEntity program;
    private final FontEntity font;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;
}
