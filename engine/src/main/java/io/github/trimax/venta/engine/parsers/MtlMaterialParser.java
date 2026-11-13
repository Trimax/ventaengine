package io.github.trimax.venta.engine.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joml.Vector2f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.MtlStatement;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.exceptions.ResourceNotFoundException;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Wavefront MTL materials: the diffuse color (`Kd`), the opacity (`d` or `Tr`), the shininess (`Ns`, as roughness),
 * the metalness (`Pm`) and the diffuse, normal and roughness maps (`map_Kd`, `map_Bump`/`bump`/`norm`, `map_Pr`).
 * Texture paths are relative to the `textures/` folder.
 */
@Slf4j
@Component
@SuppressWarnings("unused")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MtlMaterialParser {
    /** The largest usual shininess (`Ns`), it is a perfectly smooth surface */
    private static final float MAXIMAL_SHININESS = 1000.f;

    private final ResourceService resourceService;

    /** Parses the materials of a library; a missing library has no materials */
    @SneakyThrows
    public Map<String, MaterialDTO> parse(@NonNull final String resourcePath) {
        final String text;
        try {
            text = resourceService.getAsString(resourcePath);
        } catch (final ResourceNotFoundException exception) {
            log.warn("Material library {} isn't found, the parts using it have no materials", resourcePath);
            return Map.of();
        }

        try (final var reader = new BufferedReader(new StringReader(text))) {
            return parse(reader);
        }
    }

    static Map<String, MaterialDTO> parse(final BufferedReader reader) throws IOException {
        final Map<String, MaterialDTO> materials = new LinkedHashMap<>();
        Builder material = null;

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
                continue;

            final var tokens = line.split("\\s+");
            final var statement = MtlStatement.of(tokens[0]);
            if (statement == null) {
                log.debug("Unsupported MTL statement (ignored): {}", line);
                continue;
            }

            if (statement == MtlStatement.NewMaterial) {
                if (material != null)
                    materials.put(material.name, material.build());

                material = new Builder(line.substring(tokens[0].length()).trim());
                continue;
            }

            if (material == null) {
                log.debug("MTL statement before the first material (ignored): {}", line);
                continue;
            }

            switch (statement) {
                case DiffuseColor -> material.color = new ColorDTO(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]), 1.f);
                case Opacity -> material.opacity = Float.parseFloat(tokens[tokens.length - 1]);
                case Transparency -> material.opacity = 1.f - Float.parseFloat(tokens[tokens.length - 1]);
                case Shininess -> material.roughness = getRoughness(Float.parseFloat(tokens[1]));
                case Roughness -> material.roughness = Math.clamp(Float.parseFloat(tokens[1]), 0.f, 1.f);
                case Metalness -> material.metalness = Math.clamp(Float.parseFloat(tokens[1]), 0.f, 1.f);
                case DiffuseMap -> material.textures.put(TextureType.Diffuse, getTexturePath(tokens));
                case NormalMap -> material.textures.put(TextureType.Normal, getTexturePath(tokens));
                case RoughnessMap -> material.textures.put(TextureType.Roughness, getTexturePath(tokens));
                default -> throw new IllegalStateException("Unexpected MTL statement: " + statement);
            }
        }

        if (material != null)
            materials.put(material.name, material.build());

        return materials;
    }

    /** A higher shininess is a smaller and sharper highlight, so a smoother surface */
    private static float getRoughness(final float shininess) {
        return 1.f - (float) Math.sqrt(Math.clamp(shininess / MAXIMAL_SHININESS, 0.f, 1.f));
    }

    /** The file name is the last token, the options (`-bm 1`, `-s 2 2` and others) before it are ignored */
    private static String getTexturePath(final String[] tokens) {
        return tokens[tokens.length - 1].replace('\\', '/');
    }

    private static final class Builder {
        private final Map<TextureType, String> textures = new EnumMap<>(TextureType.class);
        private final String name;

        private ColorDTO color = new ColorDTO(1.f, 1.f, 1.f, 1.f);
        private float roughness = 1.f;
        private float metalness = 0.f;
        private float opacity = 1.f;

        private Builder(final String name) {
            this.name = name;
        }

        private MaterialDTO build() {
            return new MaterialDTO(metalness, roughness, opacity, color, new Vector2f(1.f), new Vector2f(0.f),
                    textures.isEmpty() ? null : Map.copyOf(textures));
        }
    }
}
