package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.SceneView;
import lombok.NonNull;

public interface SceneManager extends AbstractManager<SceneView> {
    SceneView load(@NonNull final String name);

    SceneView create(@NonNull final String name);

    SceneView getCurrent();

    void setCurrent(@NonNull final SceneView scene);
}
