package io.github.trimax.venta.engine.model.instance.implementation;

import java.util.List;

import org.joml.Vector3fc;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.hierarchy.LodLevel;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.utils.GeometryUtil;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ObjectInstanceImplementation extends AbstractInstanceImplementation implements ObjectInstance {
    private final Transform transform = new Transform();
    private final Node<MeshReference> mesh;

    /** The box of the main model: the simpler models are as big, so the object is culled and sized by it */
    private final BoundingBox box;

    /** The simpler models of the object from the nearest to the farthest, the mesh is the level 0 and is not in the list */
    private final List<LodLevel> lods;

    /** The share of the screen height below which the level i + 1 replaces the level i */
    private final float[] lodSizes;

    /** The last level that is a model: the levels after it are cards, which need the shader of the cards */
    private final int lastModelLod;

    /**
     * The level that is drawn, 0 is the main model: it is chosen every frame by
     * {@link io.github.trimax.venta.engine.helpers.LodHelper}
     */
    private int lod;

    /** The level that is fading out after a change of the level, -1 when none is */
    private int previousLod = -1;

    /** How far the change of the level has gone, from 0 to 1 */
    private float lodBlend = 1.f;

    /** Whether a surface of every level is cutout, null until it is asked: only a new material of the object changes it */
    private Boolean[] levelCutout;

    private MaterialEntityImplementation material;
    private ProgramEntityImplementation program;

    /** Whether a surface of the object is cutout, null until it is asked: only a new material of the object changes it */
    private Boolean cutout;

    /** Whether no surface of the object mixes colors, null until it is asked: only a new material of the object changes it */
    private Boolean solid;

    private DrawMode drawMode = DrawMode.Polygon;
    private boolean isVisible = true;
    private boolean isLit = true;

    ObjectInstanceImplementation(@NonNull final String name,
                                 @NonNull final ProgramEntityImplementation program,
                                 final MaterialEntityImplementation material,
                                 @NonNull final Node<MeshReference> mesh,
                                 @NonNull final GizmoInstanceImplementation gizmo) {
        this(name, program, material, mesh, List.of(), gizmo);
    }

    ObjectInstanceImplementation(@NonNull final String name,
                                 @NonNull final ProgramEntityImplementation program,
                                 final MaterialEntityImplementation material,
                                 @NonNull final Node<MeshReference> mesh,
                                 @NonNull final List<LodLevel> lods,
                                 @NonNull final GizmoInstanceImplementation gizmo) {
        super(gizmo, name);

        this.box = GeometryUtil.computeBoundingBox(mesh);
        this.material = material;
        this.program = program;
        this.mesh = mesh;
        this.lods = List.copyOf(lods);
        this.levelCutout = new Boolean[lods.size() + 1];

        this.lodSizes = new float[lods.size()];
        var lastModel = 0;
        for (int level = 0; level < lods.size(); level++) {
            lodSizes[level] = lods.get(level).screenSize();
            if (!lods.get(level).facing())
                lastModel = level + 1;
        }

        this.lastModelLod = lastModel;
    }

    /** The number of the levels of detail including the main model */
    public int getLodCount() {
        return lods.size() + 1;
    }

    /** Whether the object has simpler models */
    public boolean hasLods() {
        return !lods.isEmpty();
    }

    /** The model of a level, the level 0 is the main model */
    public Node<MeshReference> getMesh(final int level) {
        return level == 0 ? mesh : lods.get(level - 1).root();
    }

    /** Whether a level is a card turned to the camera */
    public boolean isFacing(final int level) {
        return level > 0 && lods.get(level - 1).facing();
    }

    /**
     * The level to draw as a model: a card can't be drawn as a model (it needs the shader of the cards), so what draws
     * models (the shadows, the objects that are not batched) takes the last level that is a model when the level is a card.
     */
    public int getModelLod() {
        return Math.min(lod, lastModelLod);
    }

    /**
     * Changes the level that is drawn: the level that was drawn fades out while the new one fades in.
     *
     * @param level the new level, from 0 to the number of the simpler models
     */
    public void setLod(final int level) {
        if (level < 0 || level >= getLodCount())
            throw new IllegalArgumentException(String.format("The object %s has %d levels of detail, there is no level %d", getName(), getLodCount(), level));

        if (level == lod)
            return;

        previousLod = lod;
        lod = level;
        lodBlend = 0.f;
    }

    /** Moves the change of the level on, the level that was drawn is dropped when the change is over */
    public void advanceLod(final float delta, final float duration) {
        if (previousLod < 0)
            return;

        lodBlend = duration <= 0.f ? 1.f : Math.min(1.f, lodBlend + delta / duration);
        if (lodBlend >= 1.f)
            previousLod = -1;
    }

    /** Whether the object has a cutout surface: its own material or the material of a part of its model */
    public boolean hasCutout() {
        if (cutout == null) {
            var result = false;
            for (int level = 0; level < getLodCount() && !result; level++)
                result = hasCutout(level);

            cutout = result;
        }

        return cutout;
    }

    /** Whether a surface of a level is cutout */
    public boolean hasCutout(final int level) {
        if (levelCutout[level] == null)
            levelCutout[level] = (material != null && material.isCutout()) || hasCutout(getMesh(level));

        return levelCutout[level];
    }

    private static boolean hasCutout(final Node<MeshReference> node) {
        if (node == null)
            return false;

        if (node.hasValue() && node.value().hasMaterial() && node.value().material().isCutout())
            return true;

        if (node.hasChildren())
            for (final var child : node.children())
                if (hasCutout(child))
                    return true;

        return false;
    }

    /**
     * Whether the object has no partly transparent fragments: every surface of it is opaque or cutout, or it has a cutout
     * surface (the other surfaces of such an object are drawn without sorting as well). Such an object is drawn before the
     * objects that mix colors, in any order, and can be drawn with instancing.
     */
    public boolean isSolid() {
        if (solid == null)
            solid = hasCutout() || hasOnlySolidSurfaces(material, mesh) && lods.stream().allMatch(level -> hasOnlySolidSurfaces(material, level.root()));

        return solid;
    }

    private static boolean hasOnlySolidSurfaces(final MaterialEntityImplementation material, final Node<MeshReference> node) {
        if (node == null)
            return true;

        if (node.hasValue() && node.value().hasMesh()) {
            final var surface = node.value().hasMaterial() ? node.value().material() : material;
            if (surface == null || !surface.isSolid())
                return false;
        }

        if (node.hasChildren())
            for (final var child : node.children())
                if (!hasOnlySolidSurfaces(material, child))
                    return false;

        return true;
    }

    @Override
    public boolean hasProgram() {
        return program != null;
    }

    @Override
    public Vector3fc getPosition() {
        return transform.getPosition();
    }

    @Override
    public Vector3fc getRotation() {
        return transform.getRotation();
    }

    @Override
    public Vector3fc getScale() {
        return transform.getScale();
    }

    @Override
    public void setPosition(@NonNull final Vector3fc position) {
        this.transform.setPosition(position);
    }

    @Override
    public void setRotation(@NonNull final Vector3fc angles) {
        this.transform.setRotation(angles);
    }

    @Override
    public void setScale(@NonNull final Vector3fc scale) {
        this.transform.setScale(scale);
    }

    @Override
    public void move(@NonNull final Vector3fc offset) {
        this.transform.move(offset);
    }

    @Override
    public void rotate(@NonNull final Vector3fc angles) {
        this.transform.rotate(angles);
    }

    @Override
    public void scale(@NonNull final Vector3fc factor) {
        this.transform.scale(factor);
    }

    @Override
    public void scale(final float factor) {
        this.transform.scale(factor);
    }

    @Override
    public void setDrawMode(@NonNull final DrawMode drawMode) {
        this.drawMode = drawMode;
    }

    @Override
    public void setLit(final boolean lit) {
        this.isLit = lit;
    }

    @Override
    public void setVisible(final boolean visible) {
        this.isVisible = visible;
    }

    @Override
    public void setProgram(@NonNull final ProgramEntity program) {
        if (program instanceof ProgramEntityImplementation entity)
            this.program = entity;
    }

    @Override
    public void setMaterial(@NonNull final MaterialEntity material) {
        if (material instanceof MaterialEntityImplementation entity) {
            this.material = entity;
            this.cutout = null;
            this.solid = null;
            this.levelCutout = new Boolean[getLodCount()];
        }
    }
}
