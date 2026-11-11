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

    public Attenuation(@NonNull final Attenuation attenuation) {
        this(attenuation.getConstant(), attenuation.getLinear(), attenuation.getQuadratic());
    }

    /** Attenuation used when a light doesn't define one. A zero attenuation would divide by zero in shaders */
    public static Attenuation createDefault() {
        return new Attenuation(1.0f, 0.1f, 0.01f);
    }
}
