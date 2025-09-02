package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.EmitterManager;
import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.EmitterPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.EmitterPrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmitterManagerImplementation
        extends AbstractManagerImplementation<EmitterInstanceImplementation, EmitterInstance>
        implements EmitterManager {
    private final ProgramRegistryImplementation programRegistry;
    private final TextureRegistryImplementation textureRegistry;
    private final GizmoManagerImplementation gizmoManager;
    private final Abettor abettor;

    @Override
    public EmitterInstanceImplementation create(@NonNull final String name, @NonNull final EmitterPrefab prefab) {
        if (prefab instanceof EmitterPrefabImplementation emitter)
            return create(name, emitter);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private EmitterInstanceImplementation create(@NonNull final String name, @NonNull final EmitterPrefabImplementation prefab) {
        log.info("Loading emitter {}", name);

        //TODO: change shader, change gizmo type
        return store(abettor.createEmitter(name, programRegistry.get("basic.json"), prefab,
                textureRegistry.get(prefab.getDto().texture()), gizmoManager.create("emitter", GizmoType.Light)));
    }

    @Override
    public void delete(@NonNull final EmitterInstance instance) {
        if (instance instanceof EmitterInstanceImplementation emitter)
            super.delete(emitter);
    }

    @Override
    protected void destroy(final EmitterInstanceImplementation emitter) {
        log.info("Destroying emitter {} ({})", emitter.getID(), emitter.getName());
    }
}
