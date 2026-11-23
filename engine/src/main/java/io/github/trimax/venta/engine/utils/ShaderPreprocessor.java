package io.github.trimax.venta.engine.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import io.github.trimax.venta.engine.exceptions.ShaderIncludeException;
import io.github.trimax.venta.engine.model.common.shader.ShaderSource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * GLSL has no `#include` (the extension isn't available on macOS), so the includes are expanded before compiling:
 * a line `#include "common/fog.glsl"` is replaced with the content of the file.
 *
 * <ul>
 *   <li>Paths are relative to the shaders folder, like the paths of the shaders in the program definitions.</li>
 *   <li>A file is included once per shader (like `#pragma once`), the next includes of it are skipped.</li>
 *   <li>Includes of includes are expanded, a cycle is an error with the chain of the files.</li>
 *   <li>An include in a comment is not an include.</li>
 *   <li>The included files must not have `#version`, the shader has it before the first include.</li>
 * </ul>
 */
@UtilityClass
public final class ShaderPreprocessor {
    private static final Pattern INCLUDE = Pattern.compile("^\\s*#include\\s+\"([^\"]*)\"\\s*(?://.*)?$");
    private static final Pattern VERSION = Pattern.compile("^\\s*#\\s*version\\b.*");

    /**
     * Expands the includes of a shader.
     *
     * @param path   the shader file, relative to the shaders folder
     * @param loader gives the text of a file by its path relative to the shaders folder, throws or gives null when
     *               there is no such file
     */
    public ShaderSource process(@NonNull final String path, @NonNull final Function<String, String> loader) {
        final var root = normalize(path, path);
        final var text = loader.apply(root);
        if (text == null)
            throw new ShaderIncludeException(String.format("%s isn't found", root));

        final var expansion = new Expansion(loader);
        expansion.expand(root, text, true);

        return new ShaderSource(expansion.output.toString(), List.copyOf(expansion.files));
    }

    /** A path without `.` and `..` and with single separators, a path above the shaders folder is an error */
    private String normalize(final String path, final String location) {
        final Deque<String> parts = new ArrayDeque<>();
        for (final var part : path.split("/")) {
            if (part.isEmpty() || part.equals("."))
                continue;

            if (part.equals("..")) {
                if (parts.isEmpty())
                    throw new ShaderIncludeException(String.format("%s: %s is above the shaders folder", location, path));

                parts.removeLast();
                continue;
            }

            parts.addLast(part);
        }

        if (parts.isEmpty())
            throw new ShaderIncludeException(String.format("%s: the path of the file is empty", location));

        return String.join("/", parts);
    }

    /** Whether the line ends inside a block comment, when it starts inside one or not */
    private boolean endsInBlockComment(final String line, final boolean startsInBlockComment) {
        var inside = startsInBlockComment;
        var position = 0;
        while (position < line.length()) {
            if (inside) {
                final var end = line.indexOf("*/", position);
                if (end < 0)
                    return true;

                inside = false;
                position = end + 2;
                continue;
            }

            final var lineComment = line.indexOf("//", position);
            final var blockComment = line.indexOf("/*", position);
            if (blockComment < 0 || (lineComment >= 0 && lineComment < blockComment))
                return false;

            inside = true;
            position = blockComment + 2;
        }

        return inside;
    }

    @RequiredArgsConstructor
    private static final class Expansion {
        private final Function<String, String> loader;

        private final List<String> files = new ArrayList<>();
        private final Set<String> included = new HashSet<>();
        private final Deque<String> chain = new ArrayDeque<>();
        private final StringBuilder output = new StringBuilder();

        private void expand(final String path, final String text, final boolean root) {
            final var index = files.size();
            files.add(path);
            included.add(path);
            chain.addLast(path);

            var inBlockComment = false;
            var versionFound = false;
            var number = 0;
            for (final var line : text.lines().toList()) {
                number++;

                if (!inBlockComment) {
                    final var include = INCLUDE.matcher(line);
                    if (include.matches()) {
                        if (root && !versionFound)
                            throw new ShaderIncludeException(String.format("%s:%d: an include before #version", path, number));

                        include(include.group(1), path, number, index);
                        continue;
                    }

                    if (VERSION.matcher(line).matches()) {
                        if (!root)
                            throw new ShaderIncludeException(String.format("%s:%d: #version in an included file", path, number));

                        versionFound = true;
                    }
                }

                output.append(line).append('\n');
                inBlockComment = endsInBlockComment(line, inBlockComment);
            }

            chain.removeLast();
        }

        private void include(final String target, final String from, final int number, final int fromIndex) {
            final var location = String.format("%s:%d", from, number);
            final var file = normalize(target, location);

            if (chain.contains(file))
                throw new ShaderIncludeException(String.format("%s: a cycle %s -> %s", location, String.join(" -> ", chain), file));

            if (included.contains(file)) {
                // The line stays empty, so the numbers of the next lines don't change
                output.append('\n');
                return;
            }

            final var text = load(file, location);
            output.append("#line 1 ").append(files.size()).append('\n');
            expand(file, text, false);
            output.append("#line ").append(number + 1).append(' ').append(fromIndex).append('\n');
        }

        private String load(final String file, final String location) {
            final String text;
            try {
                text = loader.apply(file);
            } catch (final RuntimeException exception) {
                throw new ShaderIncludeException(String.format("%s: %s isn't found", location, file), exception);
            }

            if (text == null)
                throw new ShaderIncludeException(String.format("%s: %s isn't found", location, file));

            return text;
        }
    }
}
