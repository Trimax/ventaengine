package io.github.trimax.venta.engine.utils;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.github.trimax.venta.engine.model.dto.MeshDTO;
import lombok.experimental.UtilityClass;
import one.util.streamex.IntStreamEx;

@UtilityClass
public final class Generator {
    private static final Random random = new Random();

    public Vector4f createRandomVector4() {
        return new Vector4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
    }

    public Vector3f createRandomVector3() {
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }

    public Vector2f createRandomVector2() {
        return new Vector2f(random.nextFloat(), random.nextFloat());
    }

    public MeshDTO.Vertex createRandomVertex() {
        return new MeshDTO.Vertex(createRandomVector3(), createRandomVector3(), createRandomVector2(), createRandomVector4());
    }

    public MeshDTO.Facet createRandomFacet() {
        return new MeshDTO.Facet(random.nextInt(), random.nextInt(), random.nextInt());
    }

    public List<MeshDTO.Vertex> createRandomVertexList(final int size) {
        return createRandomList(size, Generator::createRandomVertex);
    }

    public List<MeshDTO.Facet> createRandomFacetList(final int size) {
        return createRandomList(size, Generator::createRandomFacet);
    }

    private <T> List<T> createRandomList(final int size, final Supplier<T> objectSupplier) {
        return IntStreamEx.range(size).mapToObj(_ -> objectSupplier.get()).toList();
    }
}
