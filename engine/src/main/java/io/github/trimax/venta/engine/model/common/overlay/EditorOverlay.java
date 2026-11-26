package io.github.trimax.venta.engine.model.common.overlay;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import lombok.Getter;
import lombok.Setter;

/**
 * What an editor draws over the finished image of the scene: the grid of the ground, the marks of the things that have no shape
 * (lights, emitters, sounds), and the frame of the thing that is chosen. A game has none of it.
 */
@Getter
@Setter
public final class EditorOverlay {
    /** Whether the grid of the ground is drawn */
    private boolean grid = true;

    /** Whether the marks of the lights, the emitters, the billboards and the sounds are drawn */
    private boolean marks = true;

    /** The thing that is chosen, it gets a frame; null for none */
    private AbstractInstance selected;
}
