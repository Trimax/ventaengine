package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.view.ResourceView;
import io.github.trimax.venta.engine.utils.IdentifierUtil;
import lombok.Getter;

@Getter
public final class ResourceEntity extends AbstractEntity implements ResourceView {
    ResourceEntity() {
        super(IdentifierUtil.generate(10));
    }
}
