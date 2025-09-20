package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.implementation.EmitterPrefabImplementation;
import io.github.trimax.venta.engine.model.prefabs.implementation.GridMeshPrefabImplementation;
import io.github.trimax.venta.engine.model.prefabs.implementation.LightPrefabImplementation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

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

    public GridMeshInstanceImplementation createGridMesh(@NonNull final String name,
                                                         @NonNull final ProgramEntity program,
                                                         final MaterialEntity material, //TODO: Make it non-null
                                                         @NonNull final GridMeshPrefabImplementation mesh) {
        return new GridMeshInstanceImplementation(name, program, material, mesh);
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
                                                       @NonNull final FloatBuffer bufferMatrixModel,
                                                       @NonNull final FloatBuffer bufferColor,
                                                       final int particleVertexArrayObjectID,
                                                       final int particleVerticesBufferID,
                                                       final int particleInstanceBufferID,
                                                       final int particleFacesBufferID,
                                                       final int particleColorBufferID) {
        return new EmitterInstanceImplementation(name, prefab, texture, program, gizmo, bufferMatrixModel, bufferColor,
                particleVertexArrayObjectID, particleVerticesBufferID, particleInstanceBufferID, particleFacesBufferID,
                particleColorBufferID);
    }

    public SoundSourceInstanceImplementation createSound(@NonNull final String name,
                                                         @NonNull final SoundEntityImplementation sound,
                                                         final float volume,
                                                         final float pitch,
                                                         final boolean looping,
                                                         final int sourceID,
                                                         @NonNull final GizmoInstanceImplementation gizmo) {
        return new SoundSourceInstanceImplementation(name, sound, volume, pitch, looping, sourceID, gizmo);
    }


    public SceneInstanceImplementation createScene(@NonNull final String name) {
        return new SceneInstanceImplementation(name);
    }
}
