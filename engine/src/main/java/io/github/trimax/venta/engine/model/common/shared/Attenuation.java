package io.github.trimax.venta.engine.model.common.shared;

import io.github.trimax.venta.engine.model.dto.common.AttenuationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Attenuation {
    private float constant;
    private float linear;
    private float quadratic;

    public Attenuation(@NonNull final AttenuationDTO dto) {
        this(dto.constant(), dto.linear(), dto.quadratic());
    }
}
