package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * How the HDR colors of a scene are mapped to the colors of the window.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ToneMapping {
    /** Colors above 1 are clipped, like without HDR */
    None(0),

    /** Khronos PBR Neutral: dark and mid tones stay as authored, bright colors are compressed towards white */
    Neutral(1),

    /** The filmic ACES curve: more contrast, darker shadows, bright colors roll off smoothly */
    ACES(2);

    /** The value of the operator in the tone mapping shader */
    private final int shaderValue;
}
