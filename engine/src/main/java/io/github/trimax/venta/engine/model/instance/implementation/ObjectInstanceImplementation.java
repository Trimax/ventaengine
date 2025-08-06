package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.hierarchy.Node;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
public final class ObjectInstanceImplementation extends AbstractInstanceImplementation implements ObjectInstance {
    private final Transform transform = new Transform();
    private final Node<MeshReference> mesh;
    private final BoundingBox box;

    private ProgramEntityImplementation program;

    private DrawMode drawMode = DrawMode.Polygon;
    private boolean isVisible = true;
    private boolean isLit = true;

    public ObjectInstanceImplementation(final String name,
                                        final ProgramEntity program,
                                        final Node<MeshReference> mesh,
                                        final GizmoInstanceImplementation gizmo) {
        super(gizmo, name);

        this.mesh = mesh;
        this.box = BoundingBox.of(mesh.value().mesh().getBoundingBox());

        setProgram(program);
    }

    @Override
    public boolean hasProgram() {
        return program != null;
    }

    @Override
    public boolean hasMesh() {
        return mesh != null;
    }

    @Override
    public Vector3f getPosition() {
        return transform.getPosition();
    }

    @Override
    public Vector3f getRotation() {
        return transform.getRotation();
    }

    @Override
    public Vector3f getScale() {
        return transform.getScale();
    }

    @Override
    public void setPosition(final Vector3f position) {
        this.transform.setPosition(position);
    }

    @Override
    public void setRotation(final Vector3f angles) {
        this.transform.setRotation(angles);
    }

    @Override
    public void setScale(final Vector3f scale) {
        this.transform.setScale(scale);
    }

    @Override
    public void move(final Vector3f offset) {
        this.transform.getPosition().add(offset, this.transform.getPosition());
    }

    @Override
    public void rotate(final Vector3f angles) {
        this.transform.getRotation().add(angles, this.transform.getRotation());
    }

    @Override
    public void scale(final Vector3f factor) {
        this.transform.getScale().add(factor, this.transform.getScale());
    }

    @Override
    public void setDrawMode(final DrawMode drawMode) {
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
    public void setProgram(final ProgramEntity program) {
        if (program instanceof ProgramEntityImplementation entity)
            this.program = entity;
    }
}
