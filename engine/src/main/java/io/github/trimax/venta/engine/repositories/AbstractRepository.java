package io.github.trimax.venta.engine.repositories;

import io.github.trimax.venta.engine.model.prefabs.AbstractPrefab;
import lombok.NonNull;

public interface AbstractRepository<P extends AbstractPrefab> {
    P get(@NonNull final String resourcePath);

    /**
     * Forgets what was loaded from a file, so the next request loads the file again (an editor has changed it). The things that were made from it
     * stay as they are.
     */
    void forget(@NonNull final String resourcePath);
}
