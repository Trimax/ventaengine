package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import org.joml.Vector3f;

public interface ObjectInstance extends AbstractInstance {
    MeshInstance getMesh();

    Vector3f getPosition();

    Vector3f getRotation();

    Vector3f getScale();

    boolean isVisible();

    boolean isLit();

    boolean hasProgram();

    boolean hasMesh();

    DrawMode getDrawMode();

    ProgramEntity getProgram();

    void setPosition(final Vector3f position);

    void setRotation(final Vector3f rotation);

    void setScale(final Vector3f scale);

    void move(final Vector3f offset);

    void rotate(final Vector3f angles);

    void scale(final Vector3f factor);

    void setDrawMode(final DrawMode drawMode);

    void setLit(final boolean lit);

    void setVisible(final boolean visible);

    void setProgram(final ProgramEntity program);

    void setMesh(final MeshInstance mesh);
}
