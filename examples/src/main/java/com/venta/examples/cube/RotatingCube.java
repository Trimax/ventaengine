package com.venta.examples.cube;

import com.venta.engine.configurations.RenderConfiguration;
import com.venta.engine.configurations.WindowConfiguration;
import com.venta.engine.core.Context;
import com.venta.engine.enums.DrawMode;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.interfaces.VentaInputHandler;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

@Slf4j
public final class RotatingCube implements Venta {
    private final Vector3f angles = new Vector3f(0.f, 0.f, 0.f);
    private final InputHandler inputHandler = new InputHandler();
    private final Random random = new Random();

    private ObjectView gizmo;
    private ObjectView cube;
    private LightView light;

    @Override
    public WindowConfiguration createWindowConfiguration() {
        return new WindowConfiguration("Rotating cube", 1024, 768, false);
    }

    @Override
    public RenderConfiguration createRenderConfiguration() {
        return new RenderConfiguration(false, true);
    }

    @Override
    public VentaInputHandler createInputHandler() {
        return inputHandler;
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        log.info("Rotating cube started");

        final var scene = context.getSceneManager().getCurrent();

        cube = context.getObjectManager().load("cube");
        cube.getMesh().setMaterial(context.getMaterialManager().load("stone"));
        cube.setScale(new Vector3f(2.f));
        scene.add(cube);

        light = context.getLightManager().load("basic");
        light.setPosition(new Vector3f(2.f, 2.f, 2.f));
        scene.add(light);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));

        gizmo = context.getObjectManager().load("gizmo");
        gizmo.setDrawMode(DrawMode.Edge);
        scene.add(gizmo);

        final var cube2 = context.getObjectManager().load("cube");
        cube2.getMesh().setMaterial(context.getMaterialManager().load("stone"));
        cube2.setPosition(new Vector3f(2.f, 0.f, 2.f));
        scene.add(cube2);
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
                2.5f * (float) Math.sin(elapsedTime),
                2.5f,
                2.5f * (float) Math.cos(elapsedTime)
        ));

        if (inputHandler.isButtonPushed(GLFW_KEY_SPACE))
            light.setColor(createRandomVector3());

        gizmo.setPosition(light.getPosition());
    }

    public Vector3f createRandomVector3() {
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }
}
