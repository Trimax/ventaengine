package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderGridMeshUniform;
import io.github.trimax.venta.engine.enums.ShaderSurfaceUniform;
import io.github.trimax.venta.engine.model.common.geo.Surface;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SurfaceBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, ShaderGridMeshUniform s, final Surface surface) {

        //TODO: Don't construct each time
        bind(program.getUniformID(ShaderSurfaceUniform.Color.getUniformName(s.getUniformName())), surface.color().toVector4f());
        bind(program.getUniformID(ShaderSurfaceUniform.Threshold.getUniformName(s.getUniformName())), surface.threshold());
        bind(program.getUniformID(ShaderSurfaceUniform.Transition.getUniformName(s.getUniformName())), surface.transition());
    }
}