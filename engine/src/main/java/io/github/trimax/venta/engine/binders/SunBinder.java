package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.utils.ColorUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

/**
 * What the sky shader needs to draw the disc of the sun: where the sun is, how bright it is and how big.
 */
@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SunBinder extends AbstractBinder {
    /** The edge of the disc is soft: it starts at this share of its radius */
    private static final float EDGE_START = 0.6f;

    public void bind(final ProgramEntityImplementation program, final DirectionalLight light) {
        final var visible = light != null && light.getDiscSize() > 0.f;
        bind(program.getUniformID(ShaderUniform.UseSunDisc), visible);

        if (!visible)
            return;

        // The light goes along its direction, the sun is where it comes from
        final var toSun = light.getDirection().normalize(new Vector3f()).negate();
        bind(program.getUniformID(ShaderUniform.SunDirection), toSun);

        final var radius = (float) Math.toRadians(light.getDiscSize());
        bind(program.getUniformID(ShaderUniform.SunSize), (float) Math.cos(radius), (float) Math.cos(radius * EDGE_START));

        final var scale = light.getIntensity() * light.getDiscIntensity();
        bind(program.getUniformID(ShaderUniform.SunColor),
                ColorUtil.toLinear(light.getColor().x()) * scale,
                ColorUtil.toLinear(light.getColor().y()) * scale,
                ColorUtil.toLinear(light.getColor().z()) * scale);
    }
}
