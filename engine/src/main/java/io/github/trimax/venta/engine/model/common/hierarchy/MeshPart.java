package io.github.trimax.venta.engine.model.common.hierarchy;

import java.util.List;

import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import lombok.NonNull;

/**
 * A part of a model file (a group with a material of an OBJ file).
 *
 * @param name              the part name
 * @param mesh              the geometry of the part
 * @param materialLibraries the material libraries of the model, relative to the `meshes/` folder
 * @param material          the material name in the libraries, null when the part has no material
 */
public record MeshPart(@NonNull String name,
                       @NonNull MeshEntityImplementation mesh,
                       @NonNull List<String> materialLibraries,
                       String material) {
    public boolean hasMaterial() {
        return material != null && !materialLibraries.isEmpty();
    }
}
