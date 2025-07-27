package com.venta.engine.model.view;

import com.venta.engine.enums.DrawMode;
import org.joml.Vector3f;

public interface NewObjectView extends AbstractView {
    Vector3f getPosition();

    Vector3f getRotation();

    Vector3f getScale();

    boolean isVisible();

    boolean isLit();

    boolean hasProgram();

    DrawMode getDrawMode();

    ProgramView getProgram();

    void setPosition(final Vector3f position);

    void setRotation(final Vector3f rotation);

    void setScale(final Vector3f scale);

    void move(final Vector3f offset);

    void rotate(final Vector3f angles);

    void scale(final Vector3f factor);

    void setDrawMode(final DrawMode drawMode);

    void setLit(final boolean lit);

    void setVisible(final boolean visible);

    void setMaterial(final MaterialView material);

    void setProgram(final ProgramView program);
}
