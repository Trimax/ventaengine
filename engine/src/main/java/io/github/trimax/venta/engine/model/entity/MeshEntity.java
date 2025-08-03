package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.memory.MemoryBlock;
import io.github.trimax.venta.engine.model.geo.BoundingBox;
import io.github.trimax.venta.engine.model.view.MaterialView;
import io.github.trimax.venta.engine.model.view.MeshView;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class MeshEntity extends AbstractEntity implements MeshView {
    private final int verticesCount;
    private final int facetsCount;
    private final int edgesCount;

    private final MemoryBlock<Integer> vertexArrayObject;
    private final MemoryBlock<Integer> verticesBuffer;
    private final MemoryBlock<Integer> facetsBuffer;
    private final MemoryBlock<Integer> edgesBuffer;

    private final BoundingBox boundingBox;

    private MaterialEntity material;

    public MeshEntity(@NonNull final String name,
                      final int verticesCount,
                      final int facetsCount,
                      final int edgesCount,
                      @NonNull final MemoryBlock<Integer> vertexArrayObject,
                      @NonNull final MemoryBlock<Integer> verticesBuffer,
                      @NonNull final MemoryBlock<Integer> facetsBuffer,
                      @NonNull final MemoryBlock<Integer> edgesBuffer,
                      final BoundingBox boundingBox) {
        super(name);

        this.verticesCount = verticesCount;
        this.facetsCount = facetsCount;
        this.edgesCount = edgesCount;

        this.vertexArrayObject = vertexArrayObject;
        this.verticesBuffer = verticesBuffer;
        this.facetsBuffer = facetsBuffer;
        this.edgesBuffer = edgesBuffer;

        this.boundingBox = boundingBox;
    }

    @Override
    public void setMaterial(final MaterialView material) {
        if (material instanceof MaterialEntity entity)
            this.material = entity;
    }
}
