package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.geo.BoundingBox;
import io.github.trimax.venta.engine.model.view.MeshView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import io.github.trimax.venta.engine.model.view.ProgramView;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
public final class ObjectEntity extends AbstractEntity implements ObjectView {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);
    private final BoundingBox box;

    private DrawMode drawMode = DrawMode.Polygon;
    private boolean isVisible = true;
    private boolean isLit = true;

    private ProgramEntity program;
    private MeshEntity mesh;

    public ObjectEntity(final String name,
                        final ProgramEntity program,
                        final MeshEntity mesh,
                        final Vector3f position,
                        final Vector3f rotation,
                        final Vector3f scale,
                        final GizmoEntity gizmo) {
        super(gizmo, name);

        this.mesh = mesh;
        this.program = program;

        this.position.set(position);
        this.rotation.set(rotation);
        this.scale.set(scale);
        this.box = BoundingBox.of(mesh.getBoundingBox());
    }

    @Override
    public boolean hasProgram() {
        return program != null;
    }

    @Override
    public void setPosition(final Vector3f position) {
        this.position.set(position);
    }

    @Override
    public void setRotation(final Vector3f rotation) {
        this.rotation.set(rotation);
    }

    @Override
    public void setScale(final Vector3f scale) {
        this.scale.set(scale);
    }

    @Override
    public void move(final Vector3f offset) {
        this.position.add(offset, this.position);
    }

    @Override
    public void rotate(final Vector3f angles) {
        this.rotation.add(angles, this.rotation);
    }

    @Override
    public void scale(final Vector3f factor) {
        this.scale.add(factor, this.scale);
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
    public void setProgram(final ProgramView program) {
        if (program instanceof ProgramEntity entity)
            this.program = entity;
    }

    @Override
    public void setMesh(final MeshView mesh) {
        if (mesh instanceof MeshEntity entity)
            this.mesh = entity;
    }
}
