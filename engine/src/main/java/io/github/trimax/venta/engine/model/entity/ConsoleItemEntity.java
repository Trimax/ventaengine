package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.memory.MemoryBlock;
import io.github.trimax.venta.engine.model.view.ConsoleItemView;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ConsoleItemEntity extends AbstractEntity implements ConsoleItemView {
    private final ProgramEntity program;
    private final FontEntity font;

    private final MemoryBlock<Integer> vertexArrayObject;
    private final MemoryBlock<Integer> verticesBuffer;

    public ConsoleItemEntity(final String name,
                             @NonNull final ProgramEntity program,
                             @NonNull final FontEntity font,
                             @NonNull final MemoryBlock<Integer> vertexArrayObject,
                             @NonNull final MemoryBlock<Integer> verticesBuffer) {
        super(name);

        this.vertexArrayObject = vertexArrayObject;
        this.verticesBuffer = verticesBuffer;

        this.program = program;
        this.font = font;
    }
}
