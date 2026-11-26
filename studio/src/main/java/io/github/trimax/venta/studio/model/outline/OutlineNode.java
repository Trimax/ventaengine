package io.github.trimax.venta.studio.model.outline;

import java.util.List;

/**
 * An item of the hierarchy with what is inside it.
 */
public record OutlineNode(OutlineItem item, List<OutlineNode> children) {
    public OutlineNode {
        children = List.copyOf(children);
    }

    public OutlineNode(final OutlineItem item) {
        this(item, List.of());
    }
}
