package io.github.trimax.venta.studio.services;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.google.gson.JsonObject;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.model.common.shared.AmbientOcclusion;
import io.github.trimax.venta.engine.model.common.shared.Antialiasing;
import io.github.trimax.venta.engine.model.common.shared.Bloom;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import io.github.trimax.venta.engine.model.common.shared.LightShafts;
import io.github.trimax.venta.engine.model.common.shared.View;
import io.github.trimax.venta.engine.model.common.shared.Wind;
import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.common.shared.Cone;
import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneBillboardDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneEmitterDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneLightDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneObjectDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneSoundSourceDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneTerrainSurfaceDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneWaterSurfaceDTO;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.model.instance.implementation.AbstractInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.BillboardInstance;
import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import io.github.trimax.venta.engine.model.instance.WaterSurfaceInstance;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.utils.PlacementUtil;
import io.github.trimax.venta.studio.utils.ResourceKindUtil;
import io.github.trimax.venta.studio.model.document.SceneDocument;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.studio.model.WireframeTarget;
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
    private final ResourceService resources;
    private final StudioSession session;

    /**
     * Shows a section of the environment as the file says: the fog, the sun, the bloom, ... of the scene of the engine are made again from it.
     *
     * @param name  the name of the section in the hierarchy
     * @param scene the scene as the file says now
     */
    public void applySection(@NonNull final String name, @NonNull final SceneDTO scene) {
        final var current = context.getSceneManager().getCurrent();
        if (current == null)
            return;

        switch (name) {
            case "Sun" -> current.setDirectionalLight(Optional.ofNullable(scene.directionalLight()).map(DirectionalLight::new).orElse(null));
            case "Ambient light" -> Optional.ofNullable(scene.ambientLight()).ifPresent(color -> current.setAmbientLight(color.toVector3f()));
            case "Skybox" -> Optional.ofNullable(scene.skybox()).ifPresent(cubemap -> current.setSkybox(context.getCubemapRegistry().get(cubemap)));
            case "Fog" -> current.setFog(Optional.ofNullable(scene.fog()).map(Fog::new).orElse(null));
            case "Wind" -> current.setWind(Optional.ofNullable(scene.wind()).map(Wind::new).orElse(null));
            case "Bloom" -> current.setBloom(Optional.ofNullable(scene.bloom()).map(Bloom::new).orElse(null));
            case "Light shafts" -> current.setLightShafts(Optional.ofNullable(scene.lightShafts()).map(LightShafts::new).orElse(null));
            case "Ambient occlusion" -> current.setAmbientOcclusion(Optional.ofNullable(scene.ambientOcclusion()).map(AmbientOcclusion::new).orElse(null));
            case "Antialiasing" -> current.setAntialiasing(Optional.ofNullable(scene.antialiasing()).map(Antialiasing::new).orElse(null));
            case "View" -> current.setView(Optional.ofNullable(scene.view()).map(View::new).orElse(null));
            case "Output" -> {
                Optional.ofNullable(scene.exposure()).ifPresent(current::setExposure);
                Optional.ofNullable(scene.toneMapping()).ifPresent(current::setToneMapping);
            }
            default -> throw new IllegalArgumentException(name + " is not a section of the environment");
        }
    }

    /**
     * Shows a file of a resource as it is edited: every thing of the scene that is made of the file takes what the file says. A light
     * takes it where it is; the other things are made again from the file (the engine reads them from the contents that are given
     * instead of the file, until {@link #discard(String)}), at the same places among the things of their kind.
     *
     * @param path     the place of the file in the project, like {@code emitters/campfire.json}
     * @param value    the contents of the file
     * @param document the scene that is open
     */
    public void applyResource(@NonNull final String path, @NonNull final JsonObject value, @NonNull final SceneDocument document) {
        final var target = ResourceKindUtil.of(path).orElse(null);
        if (target == null)
            return;

        resources.override(path, value.toString().getBytes(StandardCharsets.UTF_8));

        final var users = usersOf(target, document);
        if (target.kind() == OutlineKind.Light) {
            final var dto = ParsingUtil.asObject(value.toString(), LightDTO.class);
            users.forEach(index -> {
                if (instanceOf(OutlineKind.Light, index) instanceof LightInstance light)
                    applyLight(light, dto);
            });
            return;
        }

        // The file is read before anything is taken out: one that can't be read leaves the scene as it is
        forget(target.kind(), target.file());
        load(target.kind(), target.file());

        // From the last: taking a thing out moves the ones after it
        for (int position = users.size() - 1; position >= 0; position--)
            remove(target.kind(), users.get(position));

        for (final var index : users)
            create(target.kind(), index, document.getEntry(target.kind(), index));
    }

    /** What the file at a path is again what the disk has: the engine forgets what it has made of the edited contents */
    public void discard(@NonNull final String path) {
        resources.restore(path);
        ResourceKindUtil.of(path).ifPresent(target -> forget(target.kind(), target.file()));
    }

    /** The places among the things of the kind of the things that are made of the file, in order */
    private List<Integer> usersOf(final ResourceKindUtil.Target target, final SceneDocument document) {
        final var field = PlacementUtil.fileFieldOf(target.kind());
        final var users = new ArrayList<Integer>();

        for (int index = 0; index < document.count(target.kind()); index++) {
            final var entry = document.getEntry(target.kind(), index);
            if (entry.has(field) && target.file().equals(entry.get(field).getAsString()))
                users.add(index);
        }

        return users;
    }

    /** Makes what the engine makes of the file (the prefab), so it is known to be fine */
    private void load(final OutlineKind kind, final String file) {
        switch (kind) {
            case Object -> context.getObjectRepository().get(file);
            case Light -> context.getLightRepository().get(file);
            case Emitter -> context.getEmitterRepository().get(file);
            case Billboard -> context.getBillboardRepository().get(file);
            case Sound -> context.getSoundSourceRepository().get(file);
            case Terrain -> context.getTerrainSurfaceRepository().get(file);
            case Water -> context.getWaterSurfaceRepository().get(file);
            default -> {
            }
        }
    }

    private void forget(final OutlineKind kind, final String file) {
        switch (kind) {
            case Object -> context.getObjectRepository().forget(file);
            case Light -> context.getLightRepository().forget(file);
            case Emitter -> context.getEmitterRepository().forget(file);
            case Billboard -> context.getBillboardRepository().forget(file);
            case Sound -> context.getSoundSourceRepository().forget(file);
            case Terrain -> context.getTerrainSurfaceRepository().forget(file);
            case Water -> context.getWaterSurfaceRepository().forget(file);
            default -> {
            }
        }
    }

    /** What the file of a light says, the same as the engine makes a light of the file */
    private void applyLight(final LightInstance light, final LightDTO dto) {
        final var type = Optional.ofNullable(dto.type()).orElse(LightType.Point);

        light.setColor(dto.color().toVector3f());
        light.setAttenuation(Optional.ofNullable(dto.attenuation()).map(Attenuation::new).orElseGet(Attenuation::createDefault));
        light.setCastShadows(dto.castShadows());
        light.setIntensity(dto.intensity());
        light.setRange(dto.range());
        light.setCone(type == LightType.Spot ? new Cone(dto) : null);
    }

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
        applyDrawMode(created);
    }

    /** A thing that is made while some kinds are drawn as lines is drawn as the others of its kind are */
    private void applyDrawMode(final AbstractInstance created) {
        final var target = switch (created) {
            case ObjectInstance _ -> WireframeTarget.Objects;
            case TerrainSurfaceInstance _ -> WireframeTarget.Terrain;
            case WaterSurfaceInstance _ -> WireframeTarget.Water;
            case EmitterInstance _ -> WireframeTarget.Emitters;
            case BillboardInstance _ -> WireframeTarget.Billboards;
            default -> null;
        };

        if (target != null && created instanceof AbstractInstanceImplementation instance)
            instance.setDrawMode(session.getWireframe().contains(target) ? DrawMode.Edge : DrawMode.Polygon);
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
