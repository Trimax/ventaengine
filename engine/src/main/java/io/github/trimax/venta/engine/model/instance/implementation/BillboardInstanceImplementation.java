package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.SpriteEntity;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import io.github.trimax.venta.engine.model.instance.BillboardInstance;
import io.github.trimax.venta.engine.model.prefabs.implementation.BillboardPrefabImplementation;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Getter
public final class BillboardInstanceImplementation extends AbstractInstanceImplementation implements BillboardInstance {
    private final Transform transform = new Transform();
    private final Vector2f size = new Vector2f();

    //TODO: Rethink. Prefab shouldn't be a part of instance
    private final BillboardPrefabImplementation prefab;

    private ProgramEntityImplementation program;
    private SpriteEntityImplementation sprite;

    BillboardInstanceImplementation(@NonNull final String name,
                                    @NonNull final BillboardPrefabImplementation prefab,
                                    @NonNull final ProgramEntity program,
                                    @NonNull final SpriteEntity sprite,
                                    @NonNull final Vector2fc size) {
        super(name);

        this.prefab = prefab;

        setProgram(program);
        setSprite(sprite);
        setSize(size);
    }

    @Override
    public Vector2fc getSize() {
        size.set(transform.getScale().x, transform.getScale().z);
        return size;
    }

    @Override
    public Vector3fc getPosition() {
        return transform.getPosition();
    }

    @Override
    public void setProgram(@NonNull final ProgramEntity program) {
        if (program instanceof ProgramEntityImplementation entity)
            this.program = entity;
    }

    @Override
    public void setSprite(@NonNull final SpriteEntity sprite) {
        if (sprite instanceof SpriteEntityImplementation entity)
            this.sprite = entity;
    }

    @Override
    public void setSize(@NonNull final Vector2fc size) {
        transform.setScale(new Vector3f(size.x(), 1.f, size.y()));
    }

    @Override
    public void setPosition(@NonNull final Vector3fc position) {
        transform.setPosition(position);
    }
}
