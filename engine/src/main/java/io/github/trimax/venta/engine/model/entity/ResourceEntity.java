package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.view.ResourceView;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class ResourceEntity extends AbstractEntity implements ResourceView {
    ResourceEntity() {
        super(UUID.randomUUID().toString());
    }
}
