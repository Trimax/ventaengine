package io.github.trimax.venta.studio.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.studio.model.outline.OutlineItem;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.model.outline.OutlineNode;
import io.github.trimax.venta.studio.model.outline.OutputSettings;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Makes the hierarchy of a scene from its file: the settings of the environment, then the things of the scene by their kinds.
 * The categories that have nothing in them are not in the hierarchy.
 */
@UtilityClass
public final class OutlineUtil {
    public OutlineNode build(@NonNull final String sceneName, @NonNull final SceneDTO scene) {
        final var children = new ArrayList<OutlineNode>();

        add(children, category("Environment", settings(scene)));
        add(children, category("Objects", things(scene.objects(), OutlineKind.Object, dto -> dto.name(), dto -> dto.object())));
        add(children, category("Lights", things(scene.lights(), OutlineKind.Light, dto -> dto.name(), dto -> dto.light())));
        add(children, category("Terrain", things(scene.terrainSurfaces(), OutlineKind.Terrain, dto -> dto.name(), dto -> dto.terrainSurface())));
        add(children, category("Water", things(scene.waterSurfaces(), OutlineKind.Water, dto -> dto.name(), dto -> dto.waterSurface())));
        add(children, category("Emitters", things(scene.emitters(), OutlineKind.Emitter, dto -> dto.name(), dto -> dto.emitter())));
        add(children, category("Billboards", things(scene.billboards(), OutlineKind.Billboard, dto -> dto.name(), dto -> dto.billboard())));
        add(children, category("Sounds", things(scene.soundSources(), OutlineKind.Sound, dto -> dto.name(), dto -> dto.soundSource())));

        return new OutlineNode(new OutlineItem(OutlineKind.Scene, sceneName, "", scene), children);
    }

    /**
     * The places of the things of a scene that are worth looking at: the objects, the emitters and the billboards. The ground, the
     * water, the lights and the sounds are big or far, they are looked at only when there is nothing else.
     */
    public List<Vector3f> positions(@NonNull final SceneDTO scene) {
        final var positions = new ArrayList<Vector3f>();

        collect(positions, scene.objects(), dto -> dto.position());
        collect(positions, scene.emitters(), dto -> dto.position());
        collect(positions, scene.billboards(), dto -> dto.position());
        if (!positions.isEmpty())
            return positions;

        collect(positions, scene.terrainSurfaces(), dto -> dto.position());
        collect(positions, scene.waterSurfaces(), dto -> dto.position());
        collect(positions, scene.lights(), dto -> dto.position());
        collect(positions, scene.soundSources(), dto -> dto.position());

        return positions;
    }

    /**
     * The item of a thing of a scene. Things are made from the file in its order, and may share a name, so the thing is told by its name
     * and by how many things of the kind with the same name come before it.
     *
     * @param occurrence how many things of the kind with this name come before the thing
     * @return the item, null when the scene has no such thing
     */
    public OutlineItem find(@NonNull final OutlineNode scene, @NonNull final OutlineKind kind, @NonNull final String name, final int occurrence) {
        var seen = 0;
        for (final var item : items(scene, kind)) {
            if (!item.name().equals(name))
                continue;

            if (seen++ == occurrence)
                return item;
        }

        return null;
    }

    /**
     * How many items of the kind with the same name come before the item, the number {@link #find} takes.
     */
    public int occurrence(@NonNull final OutlineNode scene, @NonNull final OutlineItem item) {
        var seen = 0;
        for (final var other : items(scene, item.kind())) {
            if (other == item)
                return seen;

            if (other.name().equals(item.name()))
                seen++;
        }

        return -1;
    }

    /** The items of a kind in the order of the file */
    public List<OutlineItem> items(@NonNull final OutlineNode scene, @NonNull final OutlineKind kind) {
        final var items = new ArrayList<OutlineItem>();
        gather(scene, kind, items);

        return items;
    }

    private void gather(final OutlineNode node, final OutlineKind kind, final List<OutlineItem> items) {
        if (node.item().kind() == kind)
            items.add(node.item());

        node.children().forEach(child -> gather(child, kind, items));
    }

    private List<OutlineNode> settings(final SceneDTO scene) {
        final var settings = new ArrayList<OutlineNode>();

        setting(settings, "Sun", scene.directionalLight());
        setting(settings, "Ambient light", scene.ambientLight());
        setting(settings, "Skybox", scene.skybox() == null ? null : new SkyboxSettings(scene.skybox()));
        setting(settings, "Fog", scene.fog());
        setting(settings, "Wind", scene.wind());
        setting(settings, "Bloom", scene.bloom());
        setting(settings, "Light shafts", scene.lightShafts());
        setting(settings, "Ambient occlusion", scene.ambientOcclusion());
        setting(settings, "Antialiasing", scene.antialiasing());
        setting(settings, "View", scene.view());
        setting(settings, "Output", scene.exposure() == null && scene.toneMapping() == null ? null : new OutputSettings(scene.exposure(), scene.toneMapping()));

        return settings;
    }

    private void setting(final List<OutlineNode> settings, final String name, final Object source) {
        if (source != null)
            settings.add(new OutlineNode(new OutlineItem(OutlineKind.Setting, name, "", source)));
    }

    private <T> List<OutlineNode> things(final List<T> dtos, final OutlineKind kind, final Function<T, String> name, final Function<T, String> detail) {
        if (dtos == null)
            return List.of();

        return dtos.stream().map(dto -> new OutlineNode(new OutlineItem(kind, name.apply(dto), detail.apply(dto), dto))).toList();
    }

    private <T> void collect(final List<Vector3f> positions, final List<T> dtos, final Function<T, Vector3f> position) {
        if (dtos != null)
            dtos.stream().map(position).forEach(positions::add);
    }

    private OutlineNode category(final String name, final List<OutlineNode> children) {
        return children.isEmpty() ? null : new OutlineNode(new OutlineItem(OutlineKind.Category, name, "", null), children);
    }

    private void add(final List<OutlineNode> nodes, final OutlineNode node) {
        if (node != null)
            nodes.add(node);
    }

    /**
     * The skybox of a scene: the file of the cube map.
     */
    public record SkyboxSettings(String cubemap) {
    }
}
