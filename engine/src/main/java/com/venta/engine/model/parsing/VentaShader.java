package com.venta.engine.model.parsing;

import java.util.Map;

public record VentaShader(String type, String path, Map<String, Integer> attributes) {
}