package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.SceneInstance;
import lombok.NonNull;

public interface SceneManager extends AbstractManager<SceneInstance> {
    SceneInstance load(@NonNull final String name);

    SceneInstance create(@NonNull final String name);

    void delete(@NonNull final SceneInstance scene);

    SceneInstance getCurrent();

    void setCurrent(@NonNull final SceneInstance scene);
}
