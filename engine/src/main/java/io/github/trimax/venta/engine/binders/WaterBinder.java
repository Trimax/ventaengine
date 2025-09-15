package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaterBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program) {
        bind(program.getUniformID(ShaderUniform.SurfaceColor.getUniformName()), new Vector3f(0.12f, 0.52f, 0.34f));
        bind(program.getUniformID(ShaderUniform.DepthColor.getUniformName()), new Vector3f(0.01f, 0.13f, 0.28f));
        bind(program.getUniformID(ShaderUniform.PeakColor.getUniformName()), new Vector3f(0.50f, 0.69f, 0.75f));


        bind(program.getUniformID(ShaderUniform.PeakThreshold.getUniformName()), 0.20f);
        bind(program.getUniformID(ShaderUniform.PeakTransition.getUniformName()), 0.12f);

        bind(program.getUniformID(ShaderUniform.DepthThreshold.getUniformName()), 0.0f);
        bind(program.getUniformID(ShaderUniform.DepthTransition.getUniformName()), 0.21f);
    }
}
