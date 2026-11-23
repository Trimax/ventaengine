package io.github.trimax.venta.engine.shaders;

import static io.github.trimax.venta.engine.shaders.ShaderResources.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.model.common.shader.ShaderSource;

/**
 * Checks the shaders of the engine without OpenGL: the includes of every program are expanded and the code that would
 * go to the compiler is scanned for the mistakes the compiler or the linker would find.
 */
final class ShaderResourcesTest {
    private static final Pattern STRUCT = Pattern.compile("^struct\\s+(\\w+)\\s*\\{.*");
    private static final Pattern UNIFORM = Pattern.compile("^uniform\\s+(\\w+)\\s+(\\w+)\\s*(?:\\[[^]]*])?\\s*;.*");
    private static final Pattern CONSTANT = Pattern.compile("^const\\s+int\\s+(\\w+)\\s*=\\s*(\\d+)\\s*;.*");
    private static final Pattern DEFINE = Pattern.compile("^#define\\s+(\\w+)\\s+(\\d+)\\s*$");
    private static final Pattern FUNCTION = Pattern.compile("^(?!return\\b)(\\w+)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{?\\s*$");
    private static final Pattern INCLUDE = Pattern.compile("^\\s*#include\\b.*");

    private static final Map<String, Integer> LIMITS = Map.of(
            "MAX_MATERIALS", DefinitionsCommon.MAX_MATERIALS,
            "MAX_LIGHTS", DefinitionsCommon.MAX_LIGHTS,
            "MAX_NOISES", DefinitionsCommon.MAX_NOISES,
            "MAX_WAVES", DefinitionsCommon.MAX_WAVES,
            "MAX_FRAMES", DefinitionsCommon.MAX_FRAMES,
            "MAX_CASCADES", DefinitionsCommon.MAX_CASCADES);

    @Test
    void testEveryProgramIsExpanded() throws Exception {
        for (final var program : programs().entrySet())
            for (final var shader : shaders(program.getValue())) {
                final var source = expand(shader);
                final var firstCodeLine = source.code().lines().filter(line -> !line.isBlank() && !line.startsWith("//")).findFirst().orElse("");

                assertEquals("#version 330 core", firstCodeLine, shader + " of " + program.getKey() + " must start with the version");
                assertTrue(source.code().lines().noneMatch(line -> INCLUDE.matcher(line).matches()),
                        shader + " must not have includes left");
            }
    }

    @Test
    void testEveryShaderHasOneMain() throws Exception {
        for (final var shader : allShaders()) {
            final var mains = scan(expand(shader)).functions.stream().filter(function -> function.startsWith("main(")).count();

            assertEquals(1, mains, shader + " must have one main function");
        }
    }

    @Test
    void testNothingIsDefinedTwice() throws Exception {
        for (final var shader : allShaders()) {
            final var scan = scan(expand(shader));

            assertNoDuplicates(shader, "structs", scan.structNames);
            assertNoDuplicates(shader, "functions", scan.functions);
            assertNoDuplicates(shader, "uniforms", scan.uniformNames);
            assertNoDuplicates(shader, "constants", scan.constantNames);
        }
    }

    @Test
    void testStagesOfAProgramAgreeOnUniformsAndStructs() throws Exception {
        for (final var program : programs().entrySet()) {
            final var vertex = scan(expand(program.getValue().shaderVertex()));
            final var fragment = scan(expand(program.getValue().shaderFragment()));

            vertex.uniformTypes.forEach((name, type) -> {
                if (fragment.uniformTypes.containsKey(name))
                    assertEquals(type, fragment.uniformTypes.get(name), "The uniform " + name + " of " + program.getKey() + " has different types");
            });
            vertex.structBodies.forEach((name, body) -> {
                if (fragment.structBodies.containsKey(name))
                    assertEquals(body, fragment.structBodies.get(name), "The struct " + name + " of " + program.getKey() + " differs between the stages");
            });
        }
    }

    @Test
    void testLimitsMatchTheEngine() throws Exception {
        final Set<String> found = new HashSet<>();
        for (final var shader : allShaders())
            scan(expand(shader)).constants.forEach((name, value) -> {
                if (LIMITS.containsKey(name)) {
                    assertEquals(LIMITS.get(name), value, name + " of " + shader + " must be the same as in DefinitionsCommon");
                    found.add(name);
                }
            });

        assertEquals(LIMITS.keySet(), found, "Every limit must be in a shader");
    }

    @Test
    void testEveryShaderFileBelongsToAProgram() throws Exception {
        final var used = programs().values().stream().flatMap(program -> shaders(program).stream()).collect(Collectors.toSet());
        final var existing = shaderFiles().stream().filter(file -> !file.startsWith("common/")).collect(Collectors.toSet());

        assertEquals(existing, used, "Shader files without a program or programs without a shader file");
    }

    @Test
    void testEveryCommonFileIsIncluded() throws Exception {
        final Set<String> included = new HashSet<>();
        for (final var shader : allShaders())
            included.addAll(expand(shader).files());

        for (final var file : shaderFiles())
            if (file.startsWith("common/"))
                assertTrue(included.contains(file), file + " isn't used by any shader");
    }

    private static void assertNoDuplicates(final String shader, final String kind, final List<String> names) {
        final var duplicates = names.stream().filter(name -> names.stream().filter(name::equals).count() > 1).distinct().toList();

        assertTrue(duplicates.isEmpty(), shader + " defines twice: " + kind + " " + duplicates);
    }

    /** The top level definitions of a shader: lines at the depth 0 outside of comments */
    private static Scan scan(final ShaderSource source) {
        final var scan = new Scan();
        var depth = 0;
        var inComment = false;
        String openStruct = null;
        final var body = new StringBuilder();

        for (final var raw : source.code().lines().toList()) {
            final var comments = stripComments(raw, inComment);
            inComment = comments.inComment;
            final var line = comments.code.strip();

            if (line.startsWith("#")) {
                final var define = DEFINE.matcher(line);
                if (define.matches())
                    scan.addConstant(define.group(1), Integer.parseInt(define.group(2)));
                continue;
            }

            if (depth == 0 && !line.isEmpty()) {
                final var struct = STRUCT.matcher(line);
                final var uniform = UNIFORM.matcher(line);
                final var constant = CONSTANT.matcher(line);
                final var function = FUNCTION.matcher(line);

                if (struct.matches()) {
                    openStruct = struct.group(1);
                    body.setLength(0);
                    scan.structNames.add(openStruct);
                } else if (uniform.matches()) {
                    scan.uniformNames.add(uniform.group(2));
                    scan.uniformTypes.put(uniform.group(2), uniform.group(1));
                } else if (constant.matches()) {
                    scan.addConstant(constant.group(1), Integer.parseInt(constant.group(2)));
                } else if (function.matches()) {
                    scan.functions.add(function.group(2) + "(" + parameterTypes(function.group(3)) + ")");
                }
            } else if (openStruct != null) {
                body.append(line).append(' ');
            }

            for (final var character : line.toCharArray()) {
                if (character == '{')
                    depth++;
                else if (character == '}')
                    depth--;
            }

            if (openStruct != null && depth == 0) {
                scan.structBodies.put(openStruct, body.toString().replaceAll("\\s+", " ").strip());
                openStruct = null;
            }
        }

        return scan;
    }

    /** The types of the parameters, their names don't make functions different */
    private static String parameterTypes(final String parameters) {
        if (parameters.isBlank() || parameters.strip().equals("void"))
            return "";

        final List<String> types = new ArrayList<>();
        for (final var parameter : parameters.split(",")) {
            final var words = parameter.strip().split("\\s+");
            types.add(String.join(" ", List.of(words).subList(0, words.length - 1)));
        }

        return String.join(",", types);
    }

    private static Comments stripComments(final String line, final boolean startsInComment) {
        final var code = new StringBuilder();
        var inComment = startsInComment;
        var position = 0;
        while (position < line.length()) {
            if (inComment) {
                final var end = line.indexOf("*/", position);
                if (end < 0)
                    return new Comments(code.toString(), true);

                inComment = false;
                position = end + 2;
                continue;
            }

            final var lineComment = line.indexOf("//", position);
            final var blockComment = line.indexOf("/*", position);
            if (blockComment >= 0 && (lineComment < 0 || blockComment < lineComment)) {
                code.append(line, position, blockComment).append(' ');
                inComment = true;
                position = blockComment + 2;
                continue;
            }

            code.append(line, position, lineComment >= 0 ? lineComment : line.length());
            break;
        }

        return new Comments(code.toString(), inComment);
    }

    private record Comments(String code, boolean inComment) {
    }

    private static final class Scan {
        private final List<String> structNames = new ArrayList<>();
        private final List<String> functions = new ArrayList<>();
        private final List<String> uniformNames = new ArrayList<>();
        private final List<String> constantNames = new ArrayList<>();
        private final Map<String, String> uniformTypes = new HashMap<>();
        private final Map<String, String> structBodies = new HashMap<>();
        private final Map<String, Integer> constants = new HashMap<>();

        private void addConstant(final String name, final int value) {
            constantNames.add(name);
            constants.put(name, value);
        }
    }
}
