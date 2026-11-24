package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.shared.Wind;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * What the vertex shaders (common/wind.glsl) need to sway a surface: the wind and the time are bound once per program
 * and frame, the material and the mesh with every draw.
 */
@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindBinder extends AbstractBinder {
    /** The wind of the scene, a scene without wind is a calm: programs keep their uniforms, so the strength is reset */
    public void bind(final ProgramEntityImplementation program, final Wind wind, final Engine.VentaTime time) {
        bind(program.getUniformID(ShaderUniform.WindStrength), wind == null ? 0.f : wind.getStrength());

        if (wind == null)
            return;

        bind(program.getUniformID(ShaderUniform.WindDirection), wind.getDirection());
        bind(program.getUniformID(ShaderUniform.WindSpeed), wind.getSpeed());
        bind(program.getUniformID(ShaderUniform.TimeElapsed), time == null ? 0.f : (float) time.getTimeElapsed());
    }

    /** How much the material follows the wind, a missing material does not */
    public void bind(final ProgramEntityImplementation program, final MaterialEntityImplementation material) {
        bind(program.getUniformID(ShaderUniform.WindInfluence), material == null ? 0.f : material.getWindInfluence());
    }

    /** The bounding box of the mesh: the surface bends the more the higher a point is between the lowest and the highest point of the box */
    public void bind(final ProgramEntityImplementation program, final MeshEntityImplementation mesh) {
        final var box = mesh.getBoundingBox();

        bind(program.getUniformID(ShaderUniform.MeshMinimum), box.min());
        bind(program.getUniformID(ShaderUniform.MeshMaximum), box.max());
    }
}
