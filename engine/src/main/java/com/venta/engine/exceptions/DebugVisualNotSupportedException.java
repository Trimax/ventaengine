package com.venta.engine.exceptions;

import com.venta.engine.model.view.AbstractView;
import lombok.NonNull;

public final class DebugVisualNotSupportedException extends AbstractVentaException {
    public DebugVisualNotSupportedException(@NonNull final AbstractView view) {
        super(String.format("The debug visual is not supported for %s", view.getClass().getSimpleName()));
    }
}
