package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.opengl.GL20C;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderType {
    Vertex(GL20C.GL_VERTEX_SHADER),
    Fragment(GL20C.GL_FRAGMENT_SHADER);

    private final int value;
}
