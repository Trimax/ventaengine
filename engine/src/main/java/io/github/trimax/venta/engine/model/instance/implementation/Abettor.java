package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.implementation.EmitterPrefabImplementation;
import io.github.trimax.venta.engine.model.prefabs.implementation.LightPrefabImplementation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.joml.Vector3f;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Abettor {
    public CameraInstanceImplementation createCamera(@NonNull final String name,
                                                     @NonNull final GizmoInstanceImplementation gizmo) {
        return new CameraInstanceImplementation(name, new Vector3f(0, 0, 3), new Vector3f(0), gizmo);
    }

    public GizmoInstanceImplementation createGizmo(@NonNull final String name,
                                                   @NonNull final ProgramEntityImplementation program,
                                                   @NonNull final MeshEntityImplementation mesh) {
        return new GizmoInstanceImplementation(name, program, mesh,
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(1.f, 1.f, 1.f));
    }

    public LightInstanceImplementation createLight(@NonNull final String name,
                                                   @NonNull final LightPrefabImplementation prefab,
                                                   @NonNull final GizmoInstanceImplementation gizmo) {
        return new LightInstanceImplementation(name, prefab, gizmo);
    }

    public ObjectInstanceImplementation createObject(@NonNull final String name,
                                                     @NonNull final ProgramEntity program,
                                                     final MaterialEntity material,
                                                     @NonNull final Node<MeshReference> mesh,
                                                     @NonNull final GizmoInstanceImplementation gizmo) {
        return new ObjectInstanceImplementation(name, program, material, mesh, gizmo);
    }

    public EmitterInstanceImplementation createEmitter(@NonNull final String name,
                                                       @NonNull final ProgramEntityImplementation program,
                                                       @NonNull final EmitterPrefabImplementation prefab,
                                                       @NonNull final TextureEntityImplementation texture,
                                                       @NonNull final GizmoInstanceImplementation gizmo,
                                                       final int particleVertexArrayObjectID,
                                                       final int particleVerticesBufferID,
                                                       final int particleFacesBufferID) {
        return new EmitterInstanceImplementation(name, prefab, texture, program, gizmo,
                particleVertexArrayObjectID, particleVerticesBufferID, particleFacesBufferID);
    }

    public SceneInstanceImplementation createScene(@NonNull final String name) {
        return new SceneInstanceImplementation(name);
    }
}
