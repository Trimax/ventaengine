package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.SpriteEntity;
import lombok.NonNull;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

public interface BillboardInstance extends AbstractInstance {
    ProgramEntity getProgram();

    SpriteEntity getSprite();

    Vector2fc getSize();

    Vector3fc getPosition();

    void setProgram(@NonNull final ProgramEntity program);

    void setSprite(@NonNull final SpriteEntity sprite);

    void setSize(@NonNull final Vector2fc size);

    void setPosition(@NonNull final Vector3fc position);
}
