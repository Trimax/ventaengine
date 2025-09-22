package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.dto.Range;
import io.github.trimax.venta.engine.model.common.dto.Variable;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.entity.implementation.*;
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

    public GridMeshPrefabImplementation createGridMesh(@NonNull final ProgramEntityImplementation program,
                                                       @NonNull final MaterialEntityImplementation material,
                                                       @NonNull final Geometry geometry,
                                                       final List<Wave> waves) {
        return new GridMeshPrefabImplementation(program, material, geometry, waves);
    }

    public ObjectPrefabImplementation createObject(final ProgramEntityImplementation program,
                                                   final MaterialEntityImplementation material,
                                                   final Node<MeshReference> root) {
        return new ObjectPrefabImplementation(program, material, root);
    }

    public EmitterPrefabImplementation createEmitter(@NonNull final TextureEntityImplementation texture,
                                                     @NonNull final Range<Float> particleSize,
                                                     @NonNull final Variable<Float> particleLifetime,
                                                     @NonNull final Variable<Vector3f> particleVelocity,
                                                     @NonNull final Variable<Float> particleAngularVelocity,
                                                     final int particlesCount,
                                                     final float emissionRate) {
        return new EmitterPrefabImplementation(texture,
                particleSize, particleLifetime, particleVelocity, particleAngularVelocity, particlesCount, emissionRate);
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
                                                         @NonNull final Geometry geometry,
                                                         @NonNull final Vector2f size) {
        return new BillboardPrefabImplementation(program, sprite, geometry, size);
    }
}
