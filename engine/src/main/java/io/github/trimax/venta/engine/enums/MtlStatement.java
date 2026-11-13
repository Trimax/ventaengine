package io.github.trimax.venta.engine.enums;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import one.util.streamex.StreamEx;

/**
 * Statements of Wavefront MTL material libraries supported by the engine.
 */
@Getter
public enum MtlStatement {
    NewMaterial("newmtl"),
    DiffuseColor("Kd"),
    Opacity("d"),
    Transparency("Tr"),
    Shininess("Ns"),
    Roughness("Pr"),
    Metalness("Pm"),
    DiffuseMap("map_Kd"),
    NormalMap("map_Bump", "map_bump", "bump", "norm"),
    RoughnessMap("map_Pr");

    private static final Map<String, MtlStatement> STATEMENTS = StreamEx.of(values())
            .flatMapToEntry(statement -> StreamEx.of(statement.keywords).toMap(keyword -> statement))
            .toMap();

    private final Set<String> keywords;

    MtlStatement(final String... keywords) {
        this.keywords = Set.of(keywords);
    }

    /** The statement of the keyword (the first token of a line), null for unsupported statements */
    public static MtlStatement of(final String keyword) {
        return STATEMENTS.get(keyword);
    }
}
