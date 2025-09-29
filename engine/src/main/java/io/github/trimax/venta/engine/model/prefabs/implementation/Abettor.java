package io.github.trimax.venta.engine.model.prefabs.implementation;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.scene.SceneBillboard;
import io.github.trimax.venta.engine.model.common.scene.SceneEmitter;
import io.github.trimax.venta.engine.model.common.scene.SceneLight;
import io.github.trimax.venta.engine.model.common.scene.SceneObject;
import io.github.trimax.venta.engine.model.common.scene.SceneSoundSource;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import io.github.trimax.venta.engine.model.common.shared.Noise;
import io.github.trimax.venta.engine.model.common.shared.Range;
import io.github.trimax.venta.engine.model.common.shared.Variable;
import io.github.trimax.venta.engine.model.common.shared.Wave;
import io.github.trimax.venta.engine.model.common.water.WaterFoam;
import io.github.trimax.venta.engine.model.common.water.WaterMaterial;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.GridMeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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

    public WaterSurfacePrefabImplementation createWaterSurface(@NonNull final GridMeshEntityImplementation gridMesh,
                                                               @NonNull final ProgramEntityImplementation program,
                                                               @NonNull final WaterMaterial waterMaterial,
                                                               @NonNull final WaterFoam foam,
                                                               final List<Noise> noises,
                                                               final List<Wave> waves) {
        return new WaterSurfacePrefabImplementation(gridMesh, program, waterMaterial, foam, noises, waves);
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

    public ScenePrefabImplementation createScene(@NonNull final List<SceneLight> lights,
                                                 @NonNull final List<SceneObject> objects,
                                                 @NonNull final List<SceneEmitter> emitters,
                                                 @NonNull final List<SceneBillboard> billboards,
                                                 @NonNull final List<SceneSoundSource> soundSources,
                                                 final CubemapEntityImplementation skybox,
                                                 final DirectionalLight directionalLight,
                                                 final Vector3f ambientLight,
                                                 final Fog fog) {
        return new ScenePrefabImplementation(lights, objects, emitters, billboards, soundSources, skybox, directionalLight, ambientLight, fog);
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
