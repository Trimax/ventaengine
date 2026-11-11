package io.github.trimax.venta.engine.events;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import lombok.NonNull;

/** Posted when an instance is deleted, before its resources are released */
public record InstanceDeletedEvent(@NonNull AbstractInstance instance) {}
