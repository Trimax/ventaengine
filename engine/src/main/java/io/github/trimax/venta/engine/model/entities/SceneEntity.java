package io.github.trimax.venta.engine.model.entities;

import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.model.view.LightView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import io.github.trimax.venta.engine.model.view.SceneView;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public final class SceneEntity extends AbstractEntity implements SceneView {
    private final Vector4f ambientLight = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
    private final List<ObjectEntity> objects = new ArrayList<>();
    private final List<LightEntity> lights = new ArrayList<>();

    public SceneEntity(@NonNull final String name) {
        super(name);
    }

    @Override
    public void setAmbientLight(final Vector4f ambientLight) {
        this.ambientLight.set(ambientLight);
    }

    @Override
    public void add(final ObjectView object) {
        if (object instanceof ObjectEntity entity)
            objects.add(entity);
    }

    @Override
    public void add(final LightView light) {
        if (this.lights.size() >= Definitions.LIGHT_MAX) {
            log.warn("There are maximum amount of lights ({}) in the scene {}", Definitions.LIGHT_MAX, this.lights.size());
            return;
        }

        if (light instanceof LightEntity entity)
            lights.add(entity);
    }
}
