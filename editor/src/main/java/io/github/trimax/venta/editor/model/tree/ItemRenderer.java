package io.github.trimax.venta.editor.model.tree;

import io.github.trimax.venta.editor.renderers.AbstractFileRenderer;
import io.github.trimax.venta.editor.renderers.ImageFileRenderer;
import io.github.trimax.venta.editor.renderers.TextFileRenderer;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.io.File;
import java.util.function.BiFunction;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ItemRenderer {
    AudioSource("Audio", GroupType.AudioSource, TextFileRenderer::new),
    Billboard("Billboard", GroupType.Billboard, TextFileRenderer::new),
    Camera("Camera", GroupType.Camera, TextFileRenderer::new),
    CubeMap("Cubemap", GroupType.CubeMap, TextFileRenderer::new),
    Light("Light", GroupType.Light, TextFileRenderer::new),
    Material("Material", GroupType.Material, TextFileRenderer::new),
    Mesh("Mesh", GroupType.Mesh, TextFileRenderer::new),
    Object("Object", GroupType.Object, TextFileRenderer::new),
    Program("Program", GroupType.Program, TextFileRenderer::new),
    Scene("Scene", GroupType.Scene, TextFileRenderer::new),
    Shader("Shader", GroupType.Shader, TextFileRenderer::new),
    Sprite("Sprite", GroupType.Sprite, ImageFileRenderer::new),
    Texture("Texture", GroupType.Texture, ImageFileRenderer::new);

    @Getter
    private final String displayName;

    @Getter
    private final GroupType groupType;
    private final BiFunction<TreeItem<Item>, VBox, AbstractFileRenderer> constructor;

    public void render(final TreeItem<Item> node, final VBox info, final File file) {
        constructor.apply(node, info).render(file);
    }

    public static ItemRenderer of(@NonNull final GroupType type) {
        return StreamEx.of(values())
                .filterBy(ItemRenderer::getGroupType, type)
                .findAny()
                .orElse(null);
    }
}