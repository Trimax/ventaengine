package io.github.trimax.venta.editor.model.tree;

import io.github.trimax.venta.editor.renderers.AbstractFileRenderer;
import io.github.trimax.venta.editor.renderers.ImageFileRenderer;
import io.github.trimax.venta.editor.renderers.TextFileRenderer;
import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.function.BiFunction;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ItemRenderer {
    AudioSource("Audio", TextFileRenderer::new),
    CubeMap("Cubemap", TextFileRenderer::new),
    Light("Light", TextFileRenderer::new),
    Material("Material", TextFileRenderer::new),
    Mesh("Mesh", TextFileRenderer::new),
    Object("Object", TextFileRenderer::new),
    Program("Program", TextFileRenderer::new),
    Scene("Scene", TextFileRenderer::new),
    Shader("Shader", TextFileRenderer::new),
    Sprite("Sprite", ImageFileRenderer::new),
    Texture("Texture", ImageFileRenderer::new);

    @Getter
    private final String displayName;
    private final BiFunction<TreeItem<Item>, VBox, AbstractFileRenderer> constructor;

    public void render(final TreeItem<Item> node, final VBox info, final File file) {
        constructor.apply(node, info).render(file);
    }
}