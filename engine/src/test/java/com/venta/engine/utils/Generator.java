package com.venta.engine.utils;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.venta.engine.model.dto.ObjectDTO;
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

    public Vector2i createRandomVector2() {
        return new Vector2i(random.nextInt(), random.nextInt());
    }

    public ObjectDTO.Vertex createRandomVertex() {
        return new ObjectDTO.Vertex(createRandomVector3(), createRandomVector3(), createRandomVector2(), createRandomVector4());
    }

    public ObjectDTO.Facet createRandomFacet() {
        return new ObjectDTO.Facet(random.nextInt(), random.nextInt(), random.nextInt());
    }

    public List<ObjectDTO.Vertex> createRandomVertexList(final int size) {
        return createRandomList(size, Generator::createRandomVertex);
    }

    public List<ObjectDTO.Facet> createRandomFacetList(final int size) {
        return createRandomList(size, Generator::createRandomFacet);
    }

    private <T> List<T> createRandomList(final int size, final Supplier<T> objectSupplier) {
        return IntStreamEx.range(size).mapToObj(_ -> objectSupplier.get()).toList();
    }
}
