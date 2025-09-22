package io.github.trimax.venta.engine.utils;

import io.github.trimax.venta.engine.layouts.AbstractVertexLayout;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;

@Slf4j
@UtilityClass
public final class VertexLayoutUtil {
    public <E extends Enum<E> & AbstractVertexLayout> void bind(@NonNull final Class<E> layout) {
        log.debug("Binding layout {}", layout.getSimpleName());
        final var stride = Float.BYTES * calculateTotalFloats(layout.getEnumConstants());

        int offset = 0;
        for (final E attribute : layout.getEnumConstants()) {
            bind(attribute, stride, offset);
            offset += Float.BYTES * attribute.getSize();
        }
    }

    private <E extends Enum<E> & AbstractVertexLayout> void bind(final E attribute, final int stride, final int offset) {
        glEnableVertexAttribArray(attribute.getLocationID());
        glVertexAttribPointer(attribute.getLocationID(), attribute.getSize(), GL_FLOAT, false, stride, offset);
        log.debug("Attribute {} has been bound to location {} (offset: {}; stride: {})",
                attribute.name(), attribute.getLocationID(), offset, stride);
    }

    public <E extends Enum<E> & AbstractVertexLayout> int calculateTotalFloats(@NonNull final Class<E> layout) {
        return calculateTotalFloats(layout.getEnumConstants());
    }

    private <E extends Enum<E> & AbstractVertexLayout> int calculateTotalFloats(final E[] constants) {
        return StreamEx.of(constants).mapToInt(AbstractVertexLayout::getSize).sum();
    }
}
