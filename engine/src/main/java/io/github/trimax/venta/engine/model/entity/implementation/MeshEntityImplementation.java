package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MeshEntityImplementation extends AbstractEntityImplementation implements MeshEntity {
    private final int verticesCount;
    private final int facetsCount;
    private final int edgesCount;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;
    private final int facetsBufferID;
    private final int edgesBufferID;

    private final BoundingBox boundingBox;
}
