package io.github.trimax.venta.editor.model.tree;

import io.github.trimax.venta.editor.renderers.AbstractFileRenderer;
import io.github.trimax.venta.editor.renderers.BinaryFileRenderer;
import io.github.trimax.venta.editor.renderers.ImageFileRenderer;
import io.github.trimax.venta.editor.renderers.TextFileRenderer;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
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
    Emitter("Emitter", GroupType.Emitter, TextFileRenderer::new),
    Font("Font", GroupType.Font, BinaryFileRenderer::new),
    GridMesh("Grid mesh", GroupType.GridMesh, TextFileRenderer::new),
    Light("Light", GroupType.Light, TextFileRenderer::new),
    Material("Material", GroupType.Material, TextFileRenderer::new),
    Mesh("Mesh", GroupType.Mesh, TextFileRenderer::new),
    Object("Object", GroupType.Object, TextFileRenderer::new),
    Program("Program", GroupType.Program, TextFileRenderer::new),
    Scene("Scene", GroupType.Scene, TextFileRenderer::new),
    Shader("Shader", GroupType.Shader, TextFileRenderer::new),
    Sound("Sound", GroupType.Sound, BinaryFileRenderer::new),
    Sprite("Sprite", GroupType.Sprite, ImageFileRenderer::new),
    TerrainSurface("Terrain surface", GroupType.TerrainSurface, TextFileRenderer::new),
    Texture("Texture", GroupType.Texture, ImageFileRenderer::new),
    TextureArray("Texture array", GroupType.TextureArray, TextFileRenderer::new),
    WaterSurface("Water surface", GroupType.WaterSurface, TextFileRenderer::new);

    @Getter
    private final String displayName;

    @Getter
    private final GroupType groupType;
    private final BiFunction<TreeItem<Resource>, VBox, AbstractFileRenderer> constructor;

    public void render(final TreeItem<Resource> node, final VBox info, final File file) {
        constructor.apply(node, info).render(file);
    }

    public static ItemRenderer of(@NonNull final GroupType type) {
        return StreamEx.of(values())
                .filterBy(ItemRenderer::getGroupType, type)
                .findAny()
                .orElse(null);
    }
}