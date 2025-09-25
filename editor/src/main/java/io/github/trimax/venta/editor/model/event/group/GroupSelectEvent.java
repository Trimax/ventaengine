package io.github.trimax.venta.editor.model.event.group;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import io.github.trimax.venta.engine.enums.ResourceType;

public record GroupSelectEvent(ResourceType type) implements AbstractEvent {
}
