package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector2f;
import org.joml.Vector3f;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextBinder extends AbstractBinder {
    private static final Vector2f ZERO = new Vector2f();

    public void bind(final ProgramEntityImplementation program, final Vector3f color) {
        if (color != null)
            bind(program.getUniformID("color"), color);

        bind(program.getUniformID("position"), ZERO);
        bind(program.getUniformID("scale"), 1f);
    }
}
