package io.github.trimax.venta.editor.events;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventBus {
    private static final com.google.common.eventbus.EventBus EVENT_BUS = new com.google.common.eventbus.EventBus();

    public static com.google.common.eventbus.EventBus getInstance() {
        return EVENT_BUS;
    }
}
