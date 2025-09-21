package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.GridMeshInstance;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class GridMeshInstanceImplementation extends AbstractInstanceImplementation implements GridMeshInstance {
    private final Transform transform = new Transform();
    private final List<Wave> waves = new ArrayList<>();
    private final Geometry geometry;

    private MaterialEntityImplementation material;
    private ProgramEntityImplementation program;

    private DrawMode drawMode = DrawMode.Polygon;
    private boolean isVisible = true;
    private boolean isLit = true;

    GridMeshInstanceImplementation(@NonNull final String name,
                                   @NonNull final ProgramEntityImplementation program,
                                   @NonNull final MaterialEntityImplementation material,
                                   @NonNull final Geometry geometry,
                                   @NonNull final List<Wave> waves) {
        super(null, name);

        this.program = program;
        this.material = material;
        this.geometry = geometry;
        this.waves.addAll(waves);
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
    public void setMaterial(final MaterialEntity material) {
        if (material instanceof MaterialEntityImplementation entity)
            this.material = entity;
    }
}
