package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.geo.Grid;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import io.github.trimax.venta.engine.model.dto.EmitterDTO;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Abettor {
    public LightPrefabImplementation createLight(@NonNull final LightType type,
                                                 @NonNull final Vector3f color,
                                                 @NonNull final Vector3f direction,
                                                 @NonNull final Attenuation attenuation,
                                                 final float intensity,
                                                 final float range,
                                                 final boolean castShadows) {
        return new LightPrefabImplementation(type, color, direction, attenuation, intensity, range, castShadows);
    }

    public GridMeshPrefabImplementation createGridMesh(final ProgramEntityImplementation program,
                                                       final List<Wave> waves,
                                                       final int verticesCount,
                                                       final int facetsCount,
                                                       final int vertexArrayObjectID,
                                                       final int verticesBufferID,
                                                       final int facetsBufferID,

                                                       final Grid grid) {
        return new GridMeshPrefabImplementation(program, waves, verticesCount, facetsCount, vertexArrayObjectID, verticesBufferID, facetsBufferID, grid);
    }

    public ObjectPrefabImplementation createObject(final ProgramEntityImplementation program,
                                                   final MaterialEntityImplementation material,
                                                   final Node<MeshReference> root) {
        return new ObjectPrefabImplementation(program, material, root);
    }

    public EmitterPrefabImplementation createEmitter(@NonNull final EmitterDTO dto) {
        return new EmitterPrefabImplementation(dto);
    }

    public ScenePrefabImplementation createScene(@NonNull final SceneDTO dto) {
        return new ScenePrefabImplementation(dto);
    }

    public SoundSourcePrefabImplementation createSound(@NonNull final SoundEntityImplementation sound,
                                                       final float volume,
                                                       final float pitch,
                                                       final boolean looping) {
        return new SoundSourcePrefabImplementation(sound, volume, pitch, looping);
    }

    public BillboardPrefabImplementation createBillboard(@NonNull final ProgramEntityImplementation program,
                                                         @NonNull final SpriteEntityImplementation sprite,
                                                         @NonNull final Vector2f size,
                                                         final int vertexArrayObjectID,
                                                         final int verticesBufferID,
                                                         final int facetsBufferID) {
        return new BillboardPrefabImplementation(program, sprite, size,
                vertexArrayObjectID, verticesBufferID, facetsBufferID);
    }
}
