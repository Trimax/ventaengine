package io.github.trimax.venta.engine.model.instance.implementation;

import java.nio.FloatBuffer;
import java.util.List;

import org.joml.Vector2fc;
import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.dto.common.Wave;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.implementation.EmitterPrefabImplementation;
import io.github.trimax.venta.engine.model.prefabs.implementation.LightPrefabImplementation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
                                                         @NonNull final ProgramEntityImplementation program,
                                                         @NonNull final MaterialEntityImplementation material,
                                                         @NonNull final Geometry geometry,
                                                         @NonNull final List<Wave> waves) {
        return new GridMeshInstanceImplementation(name, program, material, geometry, waves);
    }

    public ObjectInstanceImplementation createObject(@NonNull final String name,
                                                     @NonNull final ProgramEntityImplementation program,
                                                     final MaterialEntityImplementation material,
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

    public BillboardInstanceImplementation createBillboard(@NonNull final String name,
                                                           @NonNull final ProgramEntityImplementation program,
                                                           @NonNull final SpriteEntityImplementation sprite,
                                                           @NonNull final Geometry geometry,
                                                           @NonNull final Vector2fc size) {
        return new BillboardInstanceImplementation(name, program, sprite, geometry, size);
    }
}
