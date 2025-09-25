package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.common.shared.Wave;
import io.github.trimax.venta.engine.model.entity.GridMeshEntity;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.GridMeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.WaterSurfaceInstance;
import io.github.trimax.venta.engine.utils.WaveUtil;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class WaterSurfaceInstanceImplementation extends AbstractInstanceImplementation implements WaterSurfaceInstance {
    private final Transform transform = new Transform();
    private final List<Wave> waves = new ArrayList<>();
    private final float waveAmplitude;

    private GridMeshEntityImplementation gridMesh;
    private MaterialEntityImplementation material;
    private ProgramEntityImplementation program;

    private DrawMode drawMode = DrawMode.Polygon;
    private boolean isVisible = true;
    private boolean isLit = true;

    WaterSurfaceInstanceImplementation(@NonNull final String name,
                                       @NonNull final GridMeshEntityImplementation gridMesh,
                                       @NonNull final MaterialEntityImplementation material,
                                       @NonNull final ProgramEntityImplementation program,
                                       @NonNull final List<Wave> waves) {
        super(null, name);

        this.gridMesh = gridMesh;
        this.material = material;
        this.program = program;
        this.waves.addAll(waves);

        this.waveAmplitude = WaveUtil.getAmplitude(waves);
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
        if (material instanceof MaterialEntityImplementation entity)
            this.material = entity;
    }

    @Override
    public void setGridMesh(@NonNull final GridMeshEntity gridMesh) {
        if (gridMesh instanceof GridMeshEntityImplementation entity)
            this.gridMesh = entity;
    }
}
