package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.model.view.LightView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import io.github.trimax.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneManagerImplementation
        extends AbstractManagerImplementation<SceneManagerImplementation.SceneEntity, SceneView>
        implements SceneManager {
    @Getter
    private SceneEntity current;

    @Override
    public SceneEntity create(@NonNull final String name) {
        log.info("Creating scene {}", name);

        return store(new SceneEntity(name));
    }

    @Override
    public void setCurrent(@NonNull final SceneView scene) {
        if (scene instanceof SceneEntity entity)
            this.current = entity;
    }

    @Override
    protected void destroy(final SceneEntity scene) {
        log.info("Destroying scene {} ({})", scene.getID(), scene.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Getter
    public static final class SceneEntity extends AbstractEntity implements SceneView {
        private final Vector4f ambientLight = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
        private final List<ObjectManagerImplementation.ObjectEntity> objects = new ArrayList<>();
        private final List<LightManagerImplementation.LightEntity> lights = new ArrayList<>();

        SceneEntity(@NonNull final String name) {
            super(name);
        }

        @Override
        public void setAmbientLight(final Vector4f ambientLight) {
            this.ambientLight.set(ambientLight);
        }

        @Override
        public void add(final ObjectView object) {
            if (object instanceof ObjectManagerImplementation.ObjectEntity entity)
                objects.add(entity);
        }

        @Override
        public void add(final LightView light) {
            if (this.lights.size() >= Definitions.LIGHT_MAX) {
                log.warn("There are maximum amount of lights ({}) in the scene {}", Definitions.LIGHT_MAX, this.lights.size());
                return;
            }

            if (light instanceof LightManagerImplementation.LightEntity entity)
                lights.add(entity);
        }
    }
}
