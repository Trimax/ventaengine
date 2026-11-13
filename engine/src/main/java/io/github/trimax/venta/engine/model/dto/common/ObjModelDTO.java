package io.github.trimax.venta.engine.model.dto.common;

import java.util.List;

import lombok.NonNull;

/**
 * An OBJ model split into parts: every group or object (`g`, `o`) with every material used in it (`usemtl`)
 * is a separate mesh.
 *
 * @param parts             the parts in the order of the file
 * @param materialLibraries the MTL files of the model (`mtllib`), relative to the OBJ file
 */
public record ObjModelDTO(@NonNull List<Part> parts,
                          @NonNull List<String> materialLibraries) {
    /**
     * A part of a model.
     *
     * @param name     the group name (with the material name when the group uses several materials)
     * @param material the material name in the material libraries, null when the part has no `usemtl`
     * @param mesh     the geometry of the part
     */
    public record Part(@NonNull String name,
                       String material,
                       @NonNull MeshDTO mesh) {
    }
}
