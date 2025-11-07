package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.scene.*;
import io.github.trimax.venta.engine.model.common.shared.*;
import io.github.trimax.venta.engine.model.common.water.WaterFoam;
import io.github.trimax.venta.engine.model.common.water.WaterMaterial;
import io.github.trimax.venta.engine.model.entity.implementation.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Abettor {
    public LightPrefabImplementation createLight(@NonNull final Vector3f color,
                                                 @NonNull final Attenuation attenuation,
                                                 final boolean castShadows,
                                                 final float intensity,
                                                 final float range) {
        return new LightPrefabImplementation(color, attenuation, castShadows, intensity, range);
    }

    public WaterSurfacePrefabImplementation createWaterSurface(@NonNull final GridMeshEntityImplementation gridMesh,
                                                               @NonNull final ProgramEntityImplementation program,
                                                               @NonNull final WaterMaterial waterMaterial,
                                                               @NonNull final WaterFoam foam,
                                                               final List<Noise> noises,
                                                               final List<Wave> waves) {
        return new WaterSurfacePrefabImplementation(gridMesh, program, waterMaterial, foam, noises, waves);
    }

    public TerrainSurfacePrefabImplementation createTerrainSurface(@NonNull final GridMeshEntityImplementation gridMesh,
                                                                   @NonNull final ProgramEntityImplementation program,
                                                                   @NonNull final TextureEntityImplementation heightmap,
                                                                   @NonNull final List<MaterialEntityImplementation> materials,
                                                                   @NonNull final Map<TextureType, TextureArrayEntityImplementation> textureArrays,
                                                                   final float[] elevations,
                                                                   final float factor) {
        return new TerrainSurfacePrefabImplementation(gridMesh, program, heightmap, materials, textureArrays, elevations, factor);
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
