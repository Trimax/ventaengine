package io.github.trimax.venta.studio.model.event;

import io.github.trimax.venta.studio.model.outline.OutlineItem;

/**
 * An item of the hierarchy is chosen, or none (null).
 */
public record OutlineSelectedEvent(OutlineItem item) {
}
