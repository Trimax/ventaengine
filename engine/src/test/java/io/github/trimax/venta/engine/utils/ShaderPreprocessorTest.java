package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.exceptions.ShaderIncludeException;
import io.github.trimax.venta.engine.model.common.shader.ShaderSource;

final class ShaderPreprocessorTest {
    @Test
    void testShaderWithoutIncludesIsNotChanged() {
        final var source = process("main.glsl", Map.of("main.glsl", "#version 330 core\nvoid main() {\n}\n"));

        assertEquals("#version 330 core\nvoid main() {\n}\n", source.code(), "Nothing to expand");
        assertEquals(List.of("main.glsl"), source.files(), "Only the shader itself is a file");
    }

    @Test
    void testIncludeIsReplacedWithTheFile() {
        final var source = process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"common/fog.glsl\"\nvoid main() {\n}\n",
                "common/fog.glsl", "float fog;\n"));

        assertEquals("#version 330 core\n#line 1 1\nfloat fog;\n#line 3 0\nvoid main() {\n}\n", source.code(),
                "The file is inserted between #line directives and the numbering of the shader continues");
        assertEquals(List.of("main.glsl", "common/fog.glsl"), source.files(), "The included file gets the next index");
    }

    @Test
    void testNestedIncludes() {
        final var source = process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"a.glsl\"\nmain\n",
                "a.glsl", "a1\n#include \"b.glsl\"\na3\n",
                "b.glsl", "b1\n"));

        assertEquals("#version 330 core\n#line 1 1\na1\n#line 1 2\nb1\n#line 3 1\na3\n#line 3 0\nmain\n", source.code(),
                "Every file continues with its own numbering after the file it includes");
        assertEquals(List.of("main.glsl", "a.glsl", "b.glsl"), source.files(), "Files are indexed in the order of the includes");
    }

    @Test
    void testFileIsIncludedOnce() {
        final var source = process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"a.glsl\"\n#include \"b.glsl\"\n#include \"a.glsl\"\nmain\n",
                "a.glsl", "a\n",
                "b.glsl", "#include \"a.glsl\"\nb\n"));

        assertEquals(1, count(source.code(), "\na\n"), "A file included from several places is in the code once");
        assertEquals(List.of("main.glsl", "a.glsl", "b.glsl"), source.files(), "Every file has one index");
    }

    @Test
    void testSkippedIncludeKeepsTheLineNumbers() {
        final var source = process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"a.glsl\"\n#include \"a.glsl\"\nmain\n",
                "a.glsl", "a\n"));

        assertEquals("#version 330 core\n#line 1 1\na\n#line 3 0\n\nmain\n", source.code(),
                "The skipped include stays an empty line, so main is still the line 4");
    }

    @Test
    void testCycleIsReportedWithTheChain() {
        final var error = assertThrows(ShaderIncludeException.class, () -> process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"a.glsl\"\n",
                "a.glsl", "#include \"b.glsl\"\n",
                "b.glsl", "\n#include \"a.glsl\"\n")), "A cycle");

        assertTrue(error.getMessage().contains("b.glsl:2"), "The error has the file and the line: " + error.getMessage());
        assertTrue(error.getMessage().contains("main.glsl -> a.glsl -> b.glsl -> a.glsl"), "The error has the chain: " + error.getMessage());
    }

    @Test
    void testShaderIncludingItselfIsACycle() {
        assertThrows(ShaderIncludeException.class, () -> process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"main.glsl\"\n")), "A shader can't include itself");
    }

    @Test
    void testMissingFileIsReportedWithTheLocation() {
        final var error = assertThrows(ShaderIncludeException.class, () -> process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n\n#include \"common/none.glsl\"\n")), "A missing file");

        assertTrue(error.getMessage().contains("main.glsl:3"), "The error has the line of the include: " + error.getMessage());
        assertTrue(error.getMessage().contains("common/none.glsl"), "The error has the file: " + error.getMessage());
    }

    @Test
    void testMissingShaderIsReported() {
        assertThrows(ShaderIncludeException.class, () -> process("none.glsl", Map.of()), "A missing shader");
    }

    @Test
    void testLoaderErrorIsKeptAsTheCause() {
        final var failure = new IllegalStateException("broken");
        final var error = assertThrows(ShaderIncludeException.class, () -> PreprocessingUtil.process("main.glsl", path -> {
            if (path.equals("main.glsl"))
                return "#version 330 core\n#include \"a.glsl\"\n";

            throw failure;
        }), "A loader that throws");

        assertSame(failure, error.getCause(), "The cause of the loader is kept");
    }

    @Test
    void testPathsAreNormalized() {
        final var source = process("shaders/../main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"./common//a.glsl\"\n#include \"common/x/../a.glsl\"\n",
                "common/a.glsl", "a\n"));

        assertEquals(List.of("main.glsl", "common/a.glsl"), source.files(), "The same file by different paths is one file");
        assertEquals(1, count(source.code(), "\na\n"), "And it is included once");
    }

    @Test
    void testPathAboveTheShadersFolderIsRejected() {
        assertThrows(ShaderIncludeException.class, () -> process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"../secret.glsl\"\n")), "Above the shaders folder");
        assertThrows(ShaderIncludeException.class, () -> process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"\"\n")), "An empty path");
    }

    @Test
    void testIncludesInCommentsAreIgnored() {
        final var text = """
                #version 330 core
                // #include "line.glsl"
                /*
                #include "block.glsl"
                */
                /* a comment */ float a; // #include "tail.glsl"
                float b; /* #include "inline.glsl"
                #include "second.glsl"
                */
                """;

        final var source = process("main.glsl", Map.of("main.glsl", text));

        assertEquals(text, source.code(), "Includes in comments stay as they are");
        assertEquals(List.of("main.glsl"), source.files(), "Nothing is included");
    }

    @Test
    void testIncludeAfterAClosedBlockCommentIsExpanded() {
        final var source = process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n/*\ncomment\n*/\n#include \"a.glsl\"\n",
                "a.glsl", "a\n"));

        assertEquals(List.of("main.glsl", "a.glsl"), source.files(), "A closed comment doesn't hide the next include");
    }

    @Test
    void testDirectiveFormats() {
        final var source = process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n  \t#include   \"a.glsl\"   // the file\r\n#include\t\"b.glsl\"\r\n",
                "a.glsl", "a\r\n",
                "b.glsl", "b"));

        assertEquals(List.of("main.glsl", "a.glsl", "b.glsl"), source.files(), "Spaces, tabs and a comment after the include are allowed");
        assertFalse(source.code().contains("\r"), "Line endings are normalized");
        assertTrue(source.code().contains("\nb\n"), "A file without the last line ending is still on its own line");
    }

    @Test
    void testIncludeBeforeVersionIsRejected() {
        assertThrows(ShaderIncludeException.class, () -> process("main.glsl", Map.of(
                "main.glsl", "#include \"a.glsl\"\n#version 330 core\n",
                "a.glsl", "a\n")), "#version must be the first statement");
    }

    @Test
    void testVersionInAnIncludedFileIsRejected() {
        final var error = assertThrows(ShaderIncludeException.class, () -> process("main.glsl", Map.of(
                "main.glsl", "#version 330 core\n#include \"a.glsl\"\n",
                "a.glsl", "\n#version 330 core\n")), "A second #version");

        assertTrue(error.getMessage().contains("a.glsl:2"), "The error has the line of the version: " + error.getMessage());
    }

    @Test
    void testCommentBeforeVersionIsAllowed() {
        final var source = process("main.glsl", Map.of(
                "main.glsl", "// the shader\n#version 330 core\n#include \"a.glsl\"\n",
                "a.glsl", "a\n"));

        assertEquals(List.of("main.glsl", "a.glsl"), source.files(), "Comments before #version are fine");
    }

    @Test
    void testLogIsMappedToFiles() {
        final var source = new ShaderSource("", List.of("main.glsl", "common/fog.glsl"));

        assertEquals("ERROR: common/fog.glsl:14: 'x' : undeclared identifier\nERROR: main.glsl:3: 'y'",
                source.mapLog("ERROR: 1:14: 'x' : undeclared identifier\nERROR: 0:3: 'y'"), "Apple and ANGLE");
        assertEquals("common/fog.glsl:14(5): error: 'x' undeclared", source.mapLog("1:14(5): error: 'x' undeclared"), "Mesa");
        assertEquals("common/fog.glsl:14 : error C0000", source.mapLog("1(14) : error C0000"), "NVIDIA");
    }

    @Test
    void testUnknownFileIndexStaysInTheLog() {
        final var source = new ShaderSource("", List.of("main.glsl"));

        assertEquals("ERROR: 7:3: 'x'", source.mapLog("ERROR: 7:3: 'x'"), "An index without a file is kept");
        assertEquals("a $1 \\ b", source.mapLog("a $1 \\ b"), "Other text is kept, even with replacement characters");
    }

    private static ShaderSource process(final String path, final Map<String, String> files) {
        return PreprocessingUtil.process(path, new HashMap<>(files)::get);
    }

    private static int count(final String text, final String part) {
        var count = 0;
        for (var index = text.indexOf(part); index >= 0; index = text.indexOf(part, index + 1))
            count++;

        return count;
    }
}
