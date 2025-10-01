package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.entity.GridMeshEntity;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import lombok.NonNull;
import org.joml.Vector3fc;

public interface TerrainSurfaceInstance extends AbstractInstance {
    Vector3fc getPosition();

    Vector3fc getRotation();

    Vector3fc getScale();

    boolean isVisible();

    boolean isLit();

    boolean hasProgram();

    DrawMode getDrawMode();

    ProgramEntity getProgram();

    MaterialEntity getMaterial();

    void setPosition(@NonNull final Vector3fc position);

    void setRotation(@NonNull final Vector3fc rotation);

    void setScale(@NonNull final Vector3fc scale);

    void move(@NonNull final Vector3fc offset);

    void rotate(@NonNull final Vector3fc angles);

    void scale(@NonNull final Vector3fc factor);

    void scale(final float factor);

    void setDrawMode(@NonNull final DrawMode drawMode);

    void setLit(final boolean lit);

    void setVisible(final boolean visible);

    void setGridMesh(@NonNull final GridMeshEntity gridMesh);

    void setMaterial(@NonNull final MaterialEntity material);

    void setProgram(@NonNull final ProgramEntity program);
}
