package com.venta.engine.model;

import java.util.List;

public record VentaProgram(String name, List<String> shaders, List<String> uniforms) {
}