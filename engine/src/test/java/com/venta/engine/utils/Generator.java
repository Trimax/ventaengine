package com.venta.engine.utils;

import com.venta.engine.model.memory.Color;
import com.venta.engine.model.memory.Facet;
import com.venta.engine.model.memory.Vertex;
import lombok.experimental.UtilityClass;
import one.util.streamex.IntStreamEx;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@UtilityClass
public final class Generator {
    private static final Random random = new Random();

    public Vector3f createRandomVector3() {
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }

    public Vector2i createRandomVector2() {
        return new Vector2i(random.nextInt(), random.nextInt());
    }

    public Color createRandomColor() {
        return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
    }

    public Vertex createRandomVertex() {
        return new Vertex(createRandomVector3(), createRandomVector3(), createRandomVector2(), createRandomColor());
    }

    public Facet createRandomFacet() {
        return new Facet(random.nextInt(), random.nextInt(), random.nextInt());
    }

    public List<Vertex> createRandomVertexList(final int size) {
        return createRandomList(size, Generator::createRandomVertex);
    }

    public List<Facet> createRandomFacetList(final int size) {
        return createRandomList(size, Generator::createRandomFacet);
    }

    private <T> List<T> createRandomList(final int size, final Supplier<T> objectSupplier) {
        return IntStreamEx.range(size).mapToObj(_ -> objectSupplier.get()).toList();
    }
}
