package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.GridMeshEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import io.github.trimax.venta.engine.model.entity.implementation.*;
import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Map;

@Getter
public final class TerrainSurfaceInstanceImplementation extends AbstractInstanceImplementation implements TerrainSurfaceInstance {
    private final Transform transform = new Transform();

    private GridMeshEntityImplementation gridMesh;
    private ProgramEntityImplementation program;

    private TextureEntityImplementation heightmap;

    @NonNull
    private final List<MaterialEntityImplementation> materials;

    @NonNull
    private final Map<TextureType, TextureArrayEntityImplementation> textureArrays;

    private final float[] elevations;

    private DrawMode drawMode = DrawMode.Polygon;
    private boolean isVisible = true;
    private boolean isLit = true;

    @Setter
    private float factor;

    TerrainSurfaceInstanceImplementation(@NonNull final String name,
                                         @NonNull final GridMeshEntityImplementation gridMesh,
                                         @NonNull final ProgramEntityImplementation program,
                                         @NonNull final TextureEntityImplementation heightmap,
                                         @NonNull final List<MaterialEntityImplementation> materials,
                                         @NonNull final Map<TextureType, TextureArrayEntityImplementation> textureArrays,
                                         final float @NonNull [] elevations,
                                         final float factor) {
        super(null, name);

        this.textureArrays = textureArrays;
        this.elevations = elevations;
        this.materials = materials;
        this.heightmap = heightmap;
        this.gridMesh = gridMesh;
        this.program = program;
        this.factor = factor;
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
    public void setGridMesh(@NonNull final GridMeshEntity gridMesh) {
        if (gridMesh instanceof GridMeshEntityImplementation entity)
            this.gridMesh = entity;
    }

    @Override
    public void setHeightmap(@NonNull final TextureEntity heightmap) {
        if (heightmap instanceof TextureEntityImplementation entity)
            this.heightmap = entity;
    }
}
