package io.github.trimax.venta.engine.model.instance.implementation;

import java.util.Optional;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.prefabs.implementation.LightPrefabImplementation;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public final class LightInstanceImplementation extends AbstractInstanceImplementation implements LightInstance {
    private final Vector3f position = new Vector3f(DefinitionsCommon.VECTOR3F_ZERO);
    private final Vector3f color = new Vector3f(DefinitionsCommon.VECTOR3F_ONE);

    private Attenuation attenuation = new Attenuation(0f, 0f, 0f);
    private float intensity = 1.f;

    @Setter
    private boolean castShadows = false;

    @Setter
    private boolean enabled = true;

    LightInstanceImplementation(@NonNull final String name,
                                @NonNull final LightPrefabImplementation prefab,
                                @NonNull final GizmoInstanceImplementation gizmo) {
        super(gizmo, name);

        setAttenuation(Optional.of(prefab.getAttenuation()).orElse(new Attenuation(1.0f, 0.1f, 0.01f)));
        setCastShadows(prefab.isCastShadows());
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
    public void setIntensity(final float intensity) {
        this.intensity = intensity;
    }
}
