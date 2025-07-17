package com.venta.engine;

import com.venta.engine.core.VentaEngine;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class Application {
    public static void main(final String[] args) {
        final var engine = new VentaEngine();

        final var window = engine.getWindowManager().create("Venta engine", 1024, 768);
        engine.setWindow(window);

        //        final var program = engine.getProgramManager().link("Basic",
        //                engine.getShaderManager().loadVertexShader("basic.glsl"),
        //                engine.getShaderManager().loadFragmentShader("basic.glsl"));

        /* TODO list
         * 1. Figure out window order initialization
         * 2. Create scene manager and possibility to add objects to scene
         * 3. Create camera manager
         * 4. Console
         */

        engine.run();
    }
}
