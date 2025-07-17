package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.configuration.WindowConfiguration;
import com.venta.engine.model.BakedObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public final class Engine implements Runnable {
    private final Context context;

    @Setter
    private WindowManager.WindowEntity window;

    public void initialize(final WindowConfiguration windowConfiguration) {
        rotationAxis = randomUnitVector();
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = context.getWindowManager()
                .create(windowConfiguration.title(), windowConfiguration.width(), windowConfiguration.height());
    }

    private ProgramManager.ProgramEntity shaderProgram;
    private int vao;

    private float[] rotationAxis;
    private BakedObject cube;

    private float[] randomUnitVector() {
        final Random rand = new Random();
        final float x = rand.nextFloat() * 2 - 1; // [-1,1]
        final float y = rand.nextFloat() * 2 - 1;
        final float z = rand.nextFloat() * 2 - 1;

        final float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length == 0)
            return new float[]{1f, 0f, 0f}; // fallback

        return new float[]{x / length, y / length, z / length};
    }

    @Override
    public void run() {
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);

        shaderProgram = createShader();
        vao = createVAO();

        loop();

        //TODO: Deinitialize managers

        glfwTerminate();
    }

    private void loop() {
        var angle = 0.0f;

        glfwMakeContextCurrent(window.getId());

        final int angleLoc = glGetUniformLocation(shaderProgram.getIdAsInteger(), "angle");
        final int axisLoc = glGetUniformLocation(shaderProgram.getIdAsInteger(), "axis");

        while (!glfwWindowShouldClose(window.getId())) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderProgram.getIdAsInteger());

            glUniform1f(angleLoc, angle);
            glUniform3f(axisLoc, rotationAxis[0], rotationAxis[1], rotationAxis[2]);

            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, cube.facets().length, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);

            glfwSwapBuffers(window.getId());
            glfwPollEvents();

            angle += 0.01f;
        }
    }

    private int createVAO() {
        cube = context.getObjectManager().load("cube.json").getObject().bake();

        final int vao = glGenVertexArrays();
        final int vbo = glGenBuffers();
        final int ebo = glGenBuffers();

        glBindVertexArray(vao);

        // VBO
        final FloatBuffer vertexBuffer = memAllocFloat(cube.vertices().length);
        vertexBuffer.put(cube.vertices()).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        memFree(vertexBuffer);

        // EBO
        final IntBuffer indexBuffer = memAllocInt(cube.facets().length);
        indexBuffer.put(cube.facets()).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        memFree(indexBuffer);

        final int stride = 12 * Float.BYTES;

        // layout(location = 0) -> position
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // layout(location = 1) -> normal
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // layout(location = 2) -> texture coordinates
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // layout(location = 3) -> color
        glVertexAttribPointer(3, 4, GL_FLOAT, false, stride, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);

        glBindVertexArray(0);
        return vao;
    }

    private ProgramManager.ProgramEntity createShader() {
        return context.getProgramManager().link("Basic",
                context.getShaderManager().loadVertexShader("basic.glsl"),
                context.getShaderManager().loadFragmentShader("basic.glsl"));
    }
}

