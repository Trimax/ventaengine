package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.MeshPrefab;
import io.github.trimax.venta.engine.model.geo.BoundingBox;
import io.github.trimax.venta.engine.model.prefabs.implementation.AbstractPrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class MeshPrefabImplementation extends AbstractPrefabImplementation implements MeshPrefab {
    private final int verticesCount;
    private final int facetsCount;
    private final int edgesCount;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;
    private final int facetsBufferID;
    private final int edgesBufferID;

    private final BoundingBox boundingBox;

    private MaterialEntityImplementation material;

    public MeshPrefabImplementation(final int verticesCount,
                                    final int facetsCount,
                                    final int edgesCount,
                                    final int vertexArrayObjectID,
                                    final int verticesBufferID,
                                    final int facetsBufferID,
                                    final int edgesBufferID,
                                    final BoundingBox boundingBox) {
        this.verticesCount = verticesCount;
        this.facetsCount = facetsCount;
        this.edgesCount = edgesCount;

        this.vertexArrayObjectID = vertexArrayObjectID;
        this.verticesBufferID = verticesBufferID;
        this.facetsBufferID = facetsBufferID;
        this.edgesBufferID = edgesBufferID;

        this.boundingBox = boundingBox;
    }

    @Override
    public void setMaterial(final MaterialEntity material) {
        if (material instanceof MaterialEntityImplementation entity)
            this.material = entity;
    }

    @Override
    public boolean hasMaterial() {
        return material != null;
    }
}
