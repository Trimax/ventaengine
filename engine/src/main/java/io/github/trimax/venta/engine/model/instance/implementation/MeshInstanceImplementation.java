package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.geo.BoundingBox;
import io.github.trimax.venta.engine.model.instance.MaterialInstance;
import io.github.trimax.venta.engine.model.instance.MeshInstance;
import io.github.trimax.venta.engine.model.math.Transform;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class MeshInstanceImplementation extends AbstractInstanceImplementation implements MeshInstance {
    private final List<MeshInstanceImplementation> children = new ArrayList<>();
    private final Transform transform = new Transform();

    private final int verticesCount;
    private final int facetsCount;
    private final int edgesCount;

    private final int vertexArrayObjectID;
    private final int verticesBufferID;
    private final int facetsBufferID;
    private final int edgesBufferID;

    private final BoundingBox boundingBox;

    private MaterialInstanceImplementation material;

    public MeshInstanceImplementation(@NonNull final String name,
                                      final int verticesCount,
                                      final int facetsCount,
                                      final int edgesCount,
                                      final int vertexArrayObjectID,
                                      final int verticesBufferID,
                                      final int facetsBufferID,
                                      final int edgesBufferID,
                                      final BoundingBox boundingBox) {
        super(name);

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
    public void setMaterial(final MaterialInstance material) {
        if (material instanceof MaterialInstanceImplementation entity)
            this.material = entity;
    }

    @Override
    public boolean hasMaterial() {
        return material != null;
    }

    public void addChild(@NonNull final MeshInstanceImplementation mesh) {
        this.children.add(mesh);
    }
}
