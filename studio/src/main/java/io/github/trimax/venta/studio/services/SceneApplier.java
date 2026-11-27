package io.github.trimax.venta.studio.services;

import java.util.ArrayList;
import java.util.stream.StreamSupport;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.google.gson.JsonObject;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.model.dto.scene.SceneBillboardDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneEmitterDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneLightDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneObjectDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneSoundSourceDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneTerrainSurfaceDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneWaterSurfaceDTO;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.model.instance.BillboardInstance;
import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import io.github.trimax.venta.engine.model.instance.WaterSurfaceInstance;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.utils.EntryUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Shows a thing of the file in the scene of the engine: the thing that has the same place among the things of its kind takes the place,
 * the angles and the size from the file. The things of the scene are in the order of the file.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneApplier {
    private static final Vector3f ZERO = new Vector3f();
    private static final Vector3f ONE = new Vector3f(1.f);
    private static final Vector2f ONE_BY_ONE = new Vector2f(1.f);

    private final VentaContext context;

    /** The thing of the scene that has the place among the things of the kind, null when the scene has no such thing */
    public AbstractInstance instanceOf(@NonNull final OutlineKind kind, final int index) {
        final var things = things(kind);
        return things == null || index < 0 ? null : StreamSupport.stream(things.spliterator(), false).skip(index).findFirst().orElse(null);
    }

    /** The place of the thing among the things of its kind, -1 when it is not in the scene */
    public int indexOf(@NonNull final OutlineKind kind, @NonNull final AbstractInstance instance) {
        final var things = things(kind);
        if (things == null)
            return -1;

        var index = 0;
        for (final var thing : things) {
            if (thing == instance)
                return index;

            index++;
        }

        return -1;
    }

    private void add(final SceneInstance scene, final AbstractInstance instance) {
        switch (instance) {
            case ObjectInstance object -> scene.add(object);
            case LightInstance light -> scene.add(light);
            case EmitterInstance emitter -> scene.add(emitter);
            case BillboardInstance billboard -> scene.add(billboard);
            case SoundSourceInstance sound -> scene.add(sound);
            case TerrainSurfaceInstance terrain -> scene.add(terrain);
            case WaterSurfaceInstance water -> scene.add(water);
            default -> {
            }
        }
    }

    private Iterable<? extends AbstractInstance> things(final OutlineKind kind) {
        final var scene = context.getSceneManager().getCurrent();
        if (scene == null)
            return null;

        return switch (kind) {
            case Object -> scene.getObjects();
            case Light -> scene.getLights();
            case Emitter -> scene.getEmitters();
            case Billboard -> scene.getBillboards();
            case Sound -> scene.getSoundSources();
            case Terrain -> scene.getTerrainSurfaces();
            case Water -> scene.getWaterSurfaces();
            default -> null;
        };
    }

    /**
     * Makes the thing of the engine that the entry of the file describes, and puts it in the place among the things of its kind: the
     * things after it are taken out of the scene and put back after it, because the scene keeps its things in the order of the file.
     */
    public void create(@NonNull final OutlineKind kind, final int index, @NonNull final JsonObject entry) {
        final var scene = context.getSceneManager().getCurrent();
        final var text = entry.toString();

        final var tail = new ArrayList<AbstractInstance>();
        final var things = things(kind);
        if (things != null) {
            var position = 0;
            for (final var thing : things) {
                if (position++ >= index)
                    tail.add(thing);
            }
        }

        // What is made may fail (a file that can't be loaded): then the scene is as it was
        final AbstractInstance created = switch (kind) {
            case Object -> {
                final var dto = ParsingUtil.asObject(text, SceneObjectDTO.class);
                yield context.getObjectManager().create(dto.name(), context.getObjectRepository().get(dto.object()));
            }
            case Light -> {
                final var dto = ParsingUtil.asObject(text, SceneLightDTO.class);
                yield context.getLightManager().create(dto.name(), context.getLightRepository().get(dto.light()));
            }
            case Emitter -> {
                final var dto = ParsingUtil.asObject(text, SceneEmitterDTO.class);
                yield context.getEmitterManager().create(dto.name(), context.getEmitterRepository().get(dto.emitter()));
            }
            case Billboard -> {
                final var dto = ParsingUtil.asObject(text, SceneBillboardDTO.class);
                yield context.getBillboardManager().create(dto.name(), context.getBillboardRepository().get(dto.billboard()));
            }
            case Sound -> {
                final var dto = ParsingUtil.asObject(text, SceneSoundSourceDTO.class);
                yield context.getSoundSourceManager().create(dto.name(), context.getSoundSourceRepository().get(dto.soundSource()));
            }
            case Terrain -> {
                final var dto = ParsingUtil.asObject(text, SceneTerrainSurfaceDTO.class);
                yield context.getTerrainSurfaceManager().create(dto.name(), context.getTerrainSurfaceRepository().get(dto.terrainSurface()));
            }
            case Water -> {
                final var dto = ParsingUtil.asObject(text, SceneWaterSurfaceDTO.class);
                yield context.getWaterSurfaceManager().create(dto.name(), context.getWaterSurfaceRepository().get(dto.waterSurface()));
            }
            default -> throw new IllegalArgumentException(kind + " is not a thing of a scene");
        };

        tail.forEach(scene::remove);
        add(scene, created);
        tail.forEach(thing -> add(scene, thing));

        applyTransform(created, entry);
    }

    /** Takes the thing of the place out of the scene and releases it */
    public void remove(@NonNull final OutlineKind kind, final int index) {
        final var instance = instanceOf(kind, index);
        if (instance == null)
            return;

        switch (instance) {
            case ObjectInstance object -> context.getObjectManager().delete(object);
            case LightInstance light -> context.getLightManager().delete(light);
            case EmitterInstance emitter -> context.getEmitterManager().delete(emitter);
            case BillboardInstance billboard -> context.getBillboardManager().delete(billboard);
            case SoundSourceInstance sound -> context.getSoundSourceManager().delete(sound);
            case TerrainSurfaceInstance terrain -> context.getTerrainSurfaceManager().delete(terrain);
            case WaterSurfaceInstance water -> context.getWaterSurfaceManager().delete(water);
            default -> {
            }
        }
    }

    /** The place of the thing in the scene of the engine is what the file says */
    public void apply(@NonNull final OutlineKind kind, final int index, @NonNull final JsonObject entry) {
        final var instance = instanceOf(kind, index);
        if (instance != null)
            applyTransform(instance, entry);
    }

    private void applyTransform(final AbstractInstance instance, final JsonObject entry) {
        final var position = EntryUtil.getVector(entry, EntryUtil.POSITION, ZERO);
        final var angles = EntryUtil.getVector(entry, EntryUtil.ANGLES, ZERO);
        final var scale = EntryUtil.getVector(entry, EntryUtil.SCALE, ONE);

        switch (instance) {
            case ObjectInstance object -> {
                object.setPosition(position);
                object.setRotation(angles);
                object.setScale(scale);
            }
            case TerrainSurfaceInstance terrain -> {
                terrain.setPosition(position);
                terrain.setRotation(angles);
                terrain.setScale(scale);
            }
            case WaterSurfaceInstance water -> {
                water.setPosition(position);
                water.setRotation(angles);
                water.setScale(scale);
            }
            case LightInstance light -> {
                light.setPosition(position);
                if (EntryUtil.has(entry, EntryUtil.DIRECTION))
                    light.setDirection(EntryUtil.getVector(entry, EntryUtil.DIRECTION, ZERO));
            }
            case EmitterInstance emitter -> emitter.setPosition(position);
            case BillboardInstance billboard -> {
                billboard.setPosition(position);
                billboard.setSize(EntryUtil.getVector(entry, EntryUtil.SIZE, ONE_BY_ONE));
            }
            case SoundSourceInstance sound -> sound.setPosition(position);
            default -> {
            }
        }
    }
}
