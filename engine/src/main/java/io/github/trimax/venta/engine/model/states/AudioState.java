package io.github.trimax.venta.engine.model.states;

import lombok.Value;

@Value
public class AudioState extends AbstractState {
    long contextID;
    long deviceID;
}
