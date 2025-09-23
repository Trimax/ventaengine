package io.github.trimax.venta.engine.model.dto.scene;

import io.github.trimax.venta.engine.model.dto.common.Color;
import io.github.trimax.venta.engine.model.dto.common.Range;
import lombok.NonNull;

public record SceneFogDTO(@NonNull Range<Float> distance,
                          @NonNull Color color) {
}