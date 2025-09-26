package io.github.trimax.venta.engine.model.prefabs.implementation;

import java.util.List;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.scene.SceneBillboard;
import io.github.trimax.venta.engine.model.common.scene.SceneEmitter;
import io.github.trimax.venta.engine.model.common.scene.SceneLight;
import io.github.trimax.venta.engine.model.common.scene.SceneObject;
import io.github.trimax.venta.engine.model.common.scene.SceneSoundSource;
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

    CubemapEntityImplementation skybox;
    Vector3f ambientLight;
    Fog fog;
}