package io.github.trimax.venta.engine.enums;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import one.util.streamex.StreamEx;

/**
 * Statements of Wavefront OBJ files supported by the engine.
 */
@Getter
public enum ObjStatement {
    Position("v"),
    TextureCoordinates("vt"),
    Normal("vn"),
    Face("f"),
    Group("g", "o"),
    Material("usemtl"),
    MaterialLibrary("mtllib");

    private static final Map<String, ObjStatement> STATEMENTS = StreamEx.of(values())
            .flatMapToEntry(statement -> StreamEx.of(statement.keywords).toMap(keyword -> statement))
            .toMap();

    private final Set<String> keywords;

    ObjStatement(final String... keywords) {
        this.keywords = Set.of(keywords);
    }

    /** The statement of the keyword (the first token of a line), null for unsupported statements */
    public static ObjStatement of(final String keyword) {
        return STATEMENTS.get(keyword);
    }
}
