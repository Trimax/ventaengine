package io.github.trimax.venta.editor.utils;

import com.google.common.eventbus.EventBus;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class EventUtil {
    private final EventBus bus = new com.google.common.eventbus.EventBus();

    public void post(@NonNull final Object event) {
        log.info("Posting event: {}", event.getClass().getSimpleName());
        bus.post(event);
    }

    public void register(@NonNull final Object object) {
        log.info("Registering listener: {}", object.getClass().getSimpleName());
        bus.register(object);
    }
}
