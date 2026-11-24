package io.github.trimax.venta.engine.model.instance.implementation;

import org.joml.Vector3fc;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
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
    private final BoundingBox box;

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
        super(gizmo, name);

        this.box = GeometryUtil.computeBoundingBox(mesh);
        this.material = material;
        this.program = program;
        this.mesh = mesh;
    }

    /** Whether the object has a cutout surface: its own material or the material of a part of its model */
    public boolean hasCutout() {
        if (cutout == null)
            cutout = (material != null && material.isCutout()) || hasCutout(mesh);

        return cutout;
    }

    /**
     * Whether the object has no partly transparent fragments: every surface of it is opaque or cutout, or it has a cutout
     * surface (the other surfaces of such an object are drawn without sorting as well). Such an object is drawn before the
     * objects that mix colors, in any order, and can be drawn with instancing.
     */
    public boolean isSolid() {
        if (solid == null)
            solid = hasCutout() || isSolid(material, mesh);

        return solid;
    }

    private static boolean isSolid(final MaterialEntityImplementation material, final Node<MeshReference> node) {
        if (node == null)
            return true;

        if (node.hasValue() && node.value().hasMesh()) {
            final var surface = node.value().hasMaterial() ? node.value().material() : material;
            if (surface == null || !surface.isSolid())
                return false;
        }

        if (node.hasChildren())
            for (final var child : node.children())
                if (!isSolid(material, child))
                    return false;

        return true;
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
        }
    }
}
