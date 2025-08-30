package io.github.trimax.venta.editor.model.tree;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResourceType {
    Materials("Material"),
    Textures("Texture"),
    Programs("Program"),
    Shaders("Shader"),
    Objects("Object"),
    Scenes("Scene"),
    Lights("Light"),
    Meshes("Mesh"),
    Cubemaps("Cubemap");

    private final String displayName;
}