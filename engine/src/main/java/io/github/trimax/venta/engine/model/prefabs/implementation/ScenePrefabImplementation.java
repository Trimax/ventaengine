package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.enums.ToneMapping;

import java.util.List;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.scene.SceneBillboard;
import io.github.trimax.venta.engine.model.common.scene.SceneEmitter;
import io.github.trimax.venta.engine.model.common.scene.SceneLight;
import io.github.trimax.venta.engine.model.common.scene.SceneObject;
import io.github.trimax.venta.engine.model.common.scene.SceneSoundSource;
import io.github.trimax.venta.engine.model.common.scene.SceneTerrainSurface;
import io.github.trimax.venta.engine.model.common.scene.SceneWaterSurface;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ScenePrefabImplementation extends AbstractPrefabImplementation implements ScenePrefab {
    @NonNull
    List<SceneLight> lights;

    @NonNull
    List<SceneObject> objects;

    @NonNull
    List<SceneEmitter> emitters;

    @NonNull
    List<SceneBillboard> billboards;

    @NonNull
    List<SceneSoundSource> soundSources;

    @NonNull
    List<SceneTerrainSurface> terrainSurfaces;

    @NonNull
    List<SceneWaterSurface> waterSurfaces;

    CubemapEntityImplementation skybox;

    DirectionalLight directionalLight;
    Vector3f ambientLight;
    Fog fog;
    Float exposure;
    ToneMapping toneMapping;
}