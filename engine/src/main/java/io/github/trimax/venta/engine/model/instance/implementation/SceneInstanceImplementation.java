package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public final class SceneInstanceImplementation extends AbstractInstanceImplementation implements SceneInstance {
    private final Vector4f ambientLight = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
    private final List<ObjectInstanceImplementation> objects = new ArrayList<>();
    private final List<LightInstanceImplementation> lights = new ArrayList<>();

    SceneInstanceImplementation(@NonNull final String name) {
        super(name);
    }

    @Override
    public void setAmbientLight(final Vector4f ambientLight) {
        this.ambientLight.set(ambientLight);
    }

    @Override
    public void add(final ObjectInstance object) {
        if (object instanceof ObjectInstanceImplementation entity)
            objects.add(entity);
    }

    @Override
    public void add(final LightInstance light) {
        if (this.lights.size() >= Definitions.LIGHT_MAX) {
            log.warn("There are maximum amount of lights ({}) in the scene {}", Definitions.LIGHT_MAX, this.lights.size());
            return;
        }

        if (light instanceof LightInstanceImplementation entity)
            lights.add(entity);
    }
}
