package io.github.trimax.venta.engine.model.dto.scene;

import io.github.trimax.venta.engine.model.common.shared.Range;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import lombok.NonNull;

public record SceneFogDTO(@NonNull Range<Float> distance,
                          @NonNull ColorDTO color) {
}