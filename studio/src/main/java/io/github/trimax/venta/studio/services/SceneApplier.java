package io.github.trimax.venta.studio.services;

import java.util.stream.StreamSupport;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.google.gson.JsonObject;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
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

    /** The place of the thing in the scene of the engine is what the file says */
    public void apply(@NonNull final OutlineKind kind, final int index, @NonNull final JsonObject entry) {
        final var instance = instanceOf(kind, index);
        if (instance == null)
            return;

        final var position = EntryUtil.getVector(entry, EntryUtil.POSITION, ZERO);
        final var angles = EntryUtil.getVector(entry, EntryUtil.ANGLES, ZERO);
        final var scale = EntryUtil.getVector(entry, EntryUtil.SCALE, ONE);

        switch (instance) {
            case io.github.trimax.venta.engine.model.instance.ObjectInstance object -> {
                object.setPosition(position);
                object.setRotation(angles);
                object.setScale(scale);
            }
            case io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance terrain -> {
                terrain.setPosition(position);
                terrain.setRotation(angles);
                terrain.setScale(scale);
            }
            case io.github.trimax.venta.engine.model.instance.WaterSurfaceInstance water -> {
                water.setPosition(position);
                water.setRotation(angles);
                water.setScale(scale);
            }
            case io.github.trimax.venta.engine.model.instance.LightInstance light -> {
                light.setPosition(position);
                if (EntryUtil.has(entry, EntryUtil.DIRECTION))
                    light.setDirection(EntryUtil.getVector(entry, EntryUtil.DIRECTION, ZERO));
            }
            case io.github.trimax.venta.engine.model.instance.EmitterInstance emitter -> emitter.setPosition(position);
            case io.github.trimax.venta.engine.model.instance.BillboardInstance billboard -> {
                billboard.setPosition(position);
                billboard.setSize(EntryUtil.getVector(entry, EntryUtil.SIZE, ONE_BY_ONE));
            }
            case io.github.trimax.venta.engine.model.instance.SoundSourceInstance sound -> sound.setPosition(position);
            default -> {
            }
        }
    }
}
