package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.hierarchy.Node;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import lombok.NonNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface ObjectInstance extends AbstractInstance {
    Node<MeshReference> getMesh();

    Vector3fc getPosition();

    Vector3fc getRotation();

    Vector3fc getScale();

    boolean isVisible();

    boolean isLit();

    boolean hasProgram();

    boolean hasMesh();

    DrawMode getDrawMode();

    ProgramEntity getProgram();

    void setPosition(@NonNull final Vector3f position);

    void setRotation(@NonNull final Vector3f rotation);

    void setScale(@NonNull final Vector3f scale);

    void move(@NonNull final Vector3f offset);

    void rotate(@NonNull final Vector3f angles);

    void scale(@NonNull final Vector3f factor);

    void setDrawMode(@NonNull final DrawMode drawMode);

    void setLit(final boolean lit);

    void setVisible(final boolean visible);

    void setProgram(@NonNull final ProgramEntity program);
}
