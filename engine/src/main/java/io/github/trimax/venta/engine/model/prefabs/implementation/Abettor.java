package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.geo.Surface;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Fresnel;
import io.github.trimax.venta.engine.model.dto.EmitterDTO;
import io.github.trimax.venta.engine.model.dto.LightPrefabDTO;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Abettor {
    public LightPrefabImplementation createLight(@NonNull final LightPrefabDTO dto) {
        return new LightPrefabImplementation(dto);
    }

    public GridMeshPrefabImplementation createGridMesh(final ProgramEntity program,
                                                       final Wave wave,
                                                       final Surface surface,
                                                       final Surface trough,
                                                       final Surface peak,
                                                       final Fresnel fresnel,
                                                       final int verticesCount,
                                                       final int facetsCount,
                                                       final int vertexArrayObjectID,
                                                       final int verticesBufferID,
                                                       final int facetsBufferID) {
        return new GridMeshPrefabImplementation(program, wave, surface, trough, peak, fresnel, verticesCount, facetsCount, vertexArrayObjectID, verticesBufferID, facetsBufferID);
    }

    public ObjectPrefabImplementation createObject(final ProgramEntity program,
                                                   final MaterialEntity material,
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
}
