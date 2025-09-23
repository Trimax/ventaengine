package io.github.trimax.venta.editor.utils;

import com.google.common.eventbus.EventBus;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class EventUtil {
    private final EventBus bus = new com.google.common.eventbus.EventBus();

    public void post(@NonNull final Object event) {
        bus.post(event);
    }

    public void register(@NonNull final Object object) {
        bus.register(object);
    }
}
