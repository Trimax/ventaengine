package io.github.trimax.venta.engine.enums;

import static org.lwjgl.opengl.GL11C.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DrawMode {
    Polygon(GL_FILL),
    Edge(GL_LINE),
    Vertex(GL_POINT);

    private final int mode;
}
