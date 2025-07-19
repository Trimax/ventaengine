package com.venta.examples.cube;

import com.venta.engine.configuration.WindowConfiguration;
import com.venta.engine.core.Context;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.manager.ObjectManager;
import com.venta.engine.manager.ProgramManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RotatingCube implements Venta {
    private ObjectManager.ObjectEntity cube;

    @Override
    public WindowConfiguration createWindowConfiguration() {
        return new WindowConfiguration("Rotating cube", 1024, 768, false);
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        log.info("Rotating cube started");

        cube = context.getObjectManager().load("cube.json");

        //        final var program = createShader(context);
        //        cube.setProgramID(program.getId());

        final var scene = context.getSceneManager().create("Sample scene");
        context.getSceneManager().setCurrent(scene);
        scene.getObjects().add(cube);
    }

    @Override
    public void onUpdate(final double delta, final Context context) {
        cube.setRotation(cube.getRotation().add(0.1f, 0.2f, 0.3f));
    }

    private ProgramManager.ProgramEntity createShader(final Context context) {
        return context.getProgramManager().link("Basic",
                context.getShaderManager().load("basic_vertex"),
                context.getShaderManager().load("basic_fragment"));
    }
}
