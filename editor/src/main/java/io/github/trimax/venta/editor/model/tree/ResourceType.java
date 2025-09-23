package io.github.trimax.venta.editor.model.tree;

import java.io.File;
import java.util.function.BiFunction;

import io.github.trimax.venta.editor.renderers.AbstractFileRenderer;
import io.github.trimax.venta.editor.renderers.ImageFileRenderer;
import io.github.trimax.venta.editor.renderers.TextFileRenderer;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResourceType {
    Materials("Material", TextFileRenderer::new),
    Textures("Texture", ImageFileRenderer::new),
    Cubemaps("Cubemap", TextFileRenderer::new),
    Programs("Program", TextFileRenderer::new),
    Shaders("Shader", TextFileRenderer::new),
    Objects("Object", TextFileRenderer::new),
    Scenes("Scene", TextFileRenderer::new),
    Lights("Light", TextFileRenderer::new),
    Meshes("Mesh", TextFileRenderer::new),
    Audios("Audio", TextFileRenderer::new),
    Sprites("Sprite", ImageFileRenderer::new);

    @Getter
    private final String displayName;
    private final BiFunction<TreeItem<Item>, VBox, AbstractFileRenderer> constructor;

    public void render(final TreeItem<Item> node, final VBox info, final File file) {
        constructor.apply(node, info).render(file);
    }
}