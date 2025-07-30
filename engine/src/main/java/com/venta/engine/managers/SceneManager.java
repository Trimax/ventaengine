package com.venta.engine.managers;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import com.venta.engine.annotations.Component;
import com.venta.engine.definitions.Definitions;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneManager extends AbstractManager<SceneManager.SceneEntity, SceneView> {
    @Getter
    @Setter(onParam_ = @__(@NonNull))
    private SceneView current;

    public SceneView create(final String name) {
        log.info("Creating scene {}", name);

        return store(new SceneEntity(name));
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Override
    protected void destroy(final SceneEntity scene) {
        log.info("Destroying scene {} ({})", scene.getID(), scene.getName());
    }

    @Getter
    public static final class SceneEntity extends AbstractEntity implements SceneView {
        private final Vector4f ambientLight = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
        private final List<ObjectManager.ObjectEntity> objects = new ArrayList<>();
        private final List<LightManager.LightEntity> lights = new ArrayList<>();

        SceneEntity(@NonNull final String name) {
            super(name);
        }

        @Override
        public void setAmbientLight(final Vector4f ambientLight) {
            this.ambientLight.set(ambientLight);
        }

        @Override
        public void add(final ObjectView object) {
            if (object instanceof ObjectManager.ObjectEntity entity)
                objects.add(entity);
        }

        @Override
        public void add(final LightView light) {
            if (this.lights.size() >= Definitions.LIGHT_MAX) {
                log.warn("There are maximum amount of lights ({}) in the scene {}", Definitions.LIGHT_MAX, this.lights.size());
                return;
            }

            if (light instanceof LightManager.LightEntity entity)
                lights.add(entity);
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class SceneAccessor extends AbstractAccessor {}
}
