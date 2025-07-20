package com.venta.engine.model.dto;

import java.util.Map;

public record ShaderDTO(String type, String path, Map<String, Integer> attributes) {
}