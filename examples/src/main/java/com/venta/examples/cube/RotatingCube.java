package com.venta.examples.cube;

import static org.lwjgl.glfw.GLFW.*;

import java.util.Random;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.venta.engine.configurations.WindowConfiguration;
import com.venta.engine.core.Context;
import com.venta.engine.enums.DrawMode;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.interfaces.VentaInputHandler;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.ProgramView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RotatingCube implements Venta {
    private final Vector3f angles = new Vector3f(0.f, 0.f, 0.f);
    private final InputHandler inputHandler = new InputHandler();
    private final Random random = new Random();

    private ObjectView cube;
    private LightView light;

    @Override
    public WindowConfiguration createWindowConfiguration() {
        return new WindowConfiguration("Rotating cube", 1024, 768, false);
    }

    @Override
    public VentaInputHandler createInputHandler() {
        return inputHandler;
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        log.info("Rotating cube started");

        cube = context.getObjectManager().load("cube.json");
        cube.setMaterial(context.getMaterialManager().load("fabric.json"));

        light = context.getLightManager().load("basic.json");
        light.setPosition(new Vector3f(2.f, 2.f, 2.f));

        final var scene = context.getSceneManager().getCurrent();
        scene.add(light);

        final var origin = context.getObjectManager().load("origin.json");

        origin.setDrawMode(DrawMode.Edge);
        origin.setApplyLighting(false);
        origin.setScale(new Vector3f(100000f));

        final var program1 = createShader(context);
        final var program2 = createShader(context);

        origin.setProgram(program2);
        cube.setProgram(program2);

        scene.add(origin);
        scene.add(cube);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(3.f, 3.f, 3.f));
        camera.lookAt(new Vector3f(0.f));
    }

    private double elapsedTime = 0.0;

    @Override
    public void onUpdate(final double delta, final Context context) {
        angles.x = 0.f;
        angles.y = 0.f;

        if (inputHandler.isButtonPushed(GLFW_KEY_LEFT))
            angles.y = -0.05f;

        if (inputHandler.isButtonPushed(GLFW_KEY_RIGHT))
            angles.y = 0.05f;

        if (inputHandler.isButtonPushed(GLFW_KEY_UP))
            angles.x = -0.05f;

        if (inputHandler.isButtonPushed(GLFW_KEY_DOWN))
            angles.x = 0.05f;

        cube.rotate(angles);

        elapsedTime += delta;
        light.setPosition(new Vector3f(
                2.f * (float) Math.sin(elapsedTime),
                1.5f,
                2.f * (float) Math.cos(elapsedTime)
        ));

        if (inputHandler.isButtonPushed(GLFW_KEY_SPACE))
            light.setColor(createRandomVector4());
    }

    private ProgramView createShader(final Context context) {
        return context.getProgramManager().load("basic");
    }

    public Vector4f createRandomVector4() {
        return new Vector4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.f);
    }
}
