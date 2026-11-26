package io.github.trimax.venta.studio.model.event;

import io.github.trimax.venta.studio.model.outline.OutlineItem;

/**
 * Something other than the hierarchy chose an item (a click in the viewport): the hierarchy shows it chosen, or none (null).
 */
public record OutlineSelectionRequestEvent(OutlineItem item) {
}
