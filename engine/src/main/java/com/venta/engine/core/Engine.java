package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.configuration.WindowConfiguration;
import com.venta.engine.manager.ObjectManager;
import com.venta.engine.manager.ProgramManager;
import com.venta.engine.manager.SceneManager;
import com.venta.engine.manager.WindowManager;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

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

        GL.createCapabilities();
    }

    private ProgramManager.ProgramEntity shaderProgram;

    private float[] rotationAxis;

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
        glEnable(GL_DEPTH_TEST);

        shaderProgram = createShader();

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

            //TODO: Move to onUpdate
            glUseProgram(shaderProgram.getIdAsInteger());

            glUniform1f(angleLoc, angle);
            glUniform3f(axisLoc, rotationAxis[0], rotationAxis[1], rotationAxis[2]);

            render(context.getSceneManager().getCurrent());

            glfwSwapBuffers(window.getId());
            glfwPollEvents();

            angle += 0.01f;
        }
    }

    private void render(final SceneManager.SceneEntity scene) {
        if (scene == null)
            return;

        scene.getObjects().forEach(this::render);
    }

    private void render(final ObjectManager.ObjectEntity object) {
        glBindVertexArray(object.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.getBakedObject().facets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    private ProgramManager.ProgramEntity createShader() {
        return context.getProgramManager().link("Basic",
                context.getShaderManager().loadVertexShader("basic.glsl"),
                context.getShaderManager().loadFragmentShader("basic.glsl"));
    }
}

