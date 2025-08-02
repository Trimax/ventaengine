package io.github.trimax.venta.engine.model.entities;

import io.github.trimax.venta.engine.model.view.ConsoleItemView;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ConsoleItemEntity extends AbstractEntity implements ConsoleItemView {
    private final ProgramEntity program;
    private final FontEntity font;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;

    public ConsoleItemEntity(final String name,
                             @NonNull final ProgramEntity program,
                             @NonNull final FontEntity font,
                             final int vertexArrayObjectID, final int verticesBufferID) {
        super(name);

        this.vertexArrayObjectID = vertexArrayObjectID;
        this.verticesBufferID = verticesBufferID;

        this.program = program;
        this.font = font;
    }
}
