package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MeshEntityImplementation extends AbstractEntityImplementation implements MeshEntity {
    int verticesCount;
    int facetsCount;
    int edgesCount;

    int vertexArrayObjectID;
    int verticesBufferID;
    int facetsBufferID;
    int edgesBufferID;

    BoundingBox boundingBox;
}
