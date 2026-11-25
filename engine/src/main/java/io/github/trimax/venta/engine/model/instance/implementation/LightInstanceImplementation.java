package io.github.trimax.venta.engine.model.instance.implementation;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.common.shared.Cone;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.prefabs.implementation.LightPrefabImplementation;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public final class LightInstanceImplementation extends AbstractInstanceImplementation implements LightInstance {
    private final Vector3f position = new Vector3f(DefinitionsCommon.VECTOR3F_ZERO);
    private final Vector3f color = new Vector3f(DefinitionsCommon.VECTOR3F_ONE);

    private final Vector3f direction = new Vector3f(0.f, -1.f, 0.f);

    private Cone cone;
    private Attenuation attenuation = new Attenuation(0f, 0f, 0f);
    private float intensity = 1.f;
    private float range;

    @Setter
    private boolean castShadows = false;

    @Setter
    private boolean enabled = true;

    LightInstanceImplementation(@NonNull final String name,
                                @NonNull final LightPrefabImplementation prefab,
                                @NonNull final GizmoInstanceImplementation gizmo) {
        super(gizmo, name);

        // Each instance gets its own copy, otherwise changing one light would change all lights of the prefab
        setAttenuation(new Attenuation(prefab.getAttenuation()));
        setCastShadows(prefab.isCastShadows());
        setRange(prefab.getRange());
        setCone(prefab.getCone());
        setIntensity(prefab.getIntensity());
        setColor(prefab.getColor());
    }

    @Override
    public void setPosition(@NonNull final Vector3fc position) {
        this.position.set(position);
    }

    @Override
    public void setColor(@NonNull final Vector3fc color) {
        this.color.set(color);
    }

    @Override
    public void setAttenuation(@NonNull final Attenuation attenuation) {
        this.attenuation = attenuation;
    }

    @Override
    public LightType getType() {
        return cone == null ? LightType.Point : LightType.Spot;
    }

    @Override
    public void setCone(final Cone cone) {
        this.cone = cone;
    }

    @Override
    public void setDirection(@NonNull final Vector3fc direction) {
        final var length = direction.length();
        if (!Float.isFinite(length) || length < 1e-6f)
            throw new IllegalArgumentException("The direction of a light must not be zero, got " + direction);

        this.direction.set(direction).div(length);
    }

    @Override
    public void setRange(final float range) {
        if (!Float.isFinite(range) || range < 0.f)
            throw new IllegalArgumentException("The range of a light must not be negative, got " + range);

        this.range = range;
    }

    @Override
    public void setIntensity(final float intensity) {
        this.intensity = intensity;
    }
}
