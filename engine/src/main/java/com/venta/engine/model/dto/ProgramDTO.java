package com.venta.engine.model.dto;

import java.util.List;

public record ProgramDTO(String name,
                         List<String> shaders,
                         List<String> uniforms) {
}