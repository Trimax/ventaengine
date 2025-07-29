package com.venta.engine.helpers;

import org.joml.Vector3f;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.DebugVisualType;
import com.venta.engine.exceptions.DebugVisualNotSupportedException;
import com.venta.engine.managers.LightManager;
import com.venta.engine.managers.MeshManager;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.managers.ProgramManager;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.model.view.AbstractView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugHelper {
    private final ObjectManager.ObjectAccessor objectAccessor;
    private final ProgramManager programManager;
    private final ObjectManager objectManager;
    private final MeshManager meshManager;

    public ObjectManager.ObjectEntity create(final AbstractView view) {
        if (view instanceof LightManager.LightEntity light)
            return create(light.getID(), DebugVisualType.Gizmo);

        if (view instanceof SceneManager.SceneEntity scene) {
            final var origin = create(scene.getID(), DebugVisualType.Origin);
            origin.setScale(new Vector3f(100000f));
            return origin;
        }

        throw new DebugVisualNotSupportedException(view);
    }

    private ObjectManager.ObjectEntity create(final String id, final DebugVisualType debugVisualType) {
        return objectAccessor.get(objectManager.create(String.format("%s-%s", debugVisualType.name(), id),
                meshManager.load(debugVisualType.name().toLowerCase()),
                programManager.load(debugVisualType.getProgram())));
    }

    @Data
    @RequiredArgsConstructor
    public static final class DebugVisual {
        private final AbstractView referencedObject;
        private final Vector3f position;
        private final Vector3f rotation;
        private final Vector3f scale;

        private ObjectManager.ObjectEntity renderableObject;

        public boolean hasRenderableObject() {
            return renderableObject != null;
        }
    }
}
