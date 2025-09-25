package io.github.trimax.venta.editor.model.event.group;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import io.github.trimax.venta.engine.enums.GroupType;

public record GroupSelectEvent(GroupType type) implements AbstractEvent {
}
