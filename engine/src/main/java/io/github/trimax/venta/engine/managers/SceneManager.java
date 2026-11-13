package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import lombok.NonNull;

public interface SceneManager extends AbstractManager<SceneInstance> {
    SceneInstance create(@NonNull final String name,
                         @NonNull final ScenePrefab prefab);

    SceneInstance create(@NonNull final String name);

    void delete(@NonNull final SceneInstance scene);

    SceneInstance getCurrent();

    /**
     * Makes the scene current. The sound sources of the previous scene stop, the looping ones of the new scene
     * (the ambience: a campfire, wind, music) start; other sounds are played by the application.
     */
    void setCurrent(@NonNull final SceneInstance scene);
}
