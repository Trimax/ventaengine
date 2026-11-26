package io.github.trimax.venta.engine.interfaces;

/**
 * Called when a frame is drawn, before it is shown: the framebuffer of the window holds the finished image, so a host that
 * embeds the engine (an editor) can read it.
 */
@FunctionalInterface
public interface VentaEngineFrameListener {
    /**
     * A frame is drawn and not shown yet.
     *
     * @param width  the width of the image in pixels
     * @param height the height of the image in pixels
     */
    void onFrame(int width, int height);
}
