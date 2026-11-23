package io.github.trimax.venta.engine.model.common.shader;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

/**
 * The code of a shader with its includes expanded.
 *
 * @param code  the code to compile; every included file starts with a `#line 1 N` directive, where N is the index of
 *              the file in {@code files}, and the including file continues with its own index after it
 * @param files the shader file and the included files by the indexes used in the `#line` directives of the code
 */
public record ShaderSource(@NonNull String code, @NonNull List<String> files) {
    /* "ERROR: 0:12:" (Apple, ANGLE), "0:12(5): error:" (Mesa) and "0(12) : error" (NVIDIA) */
    private static final Pattern LOG_APPLE = Pattern.compile("(ERROR|WARNING): (\\d+):(\\d+):");
    private static final Pattern LOG_MESA = Pattern.compile("(?m)^(\\d+):(\\d+)\\((\\d+)\\):");
    private static final Pattern LOG_NVIDIA = Pattern.compile("(?m)^(\\d+)\\((\\d+)\\) : ");

    /**
     * Replaces the source string numbers of a compiler log with the names of the files, so an error points to the
     * line of the file it is in and not to the line of the code with the includes expanded.
     */
    public String mapLog(@NonNull final String log) {
        var result = LOG_APPLE.matcher(log).replaceAll(match ->
                Matcher.quoteReplacement(match.group(1) + ": " + describe(match.group(2), match.group(3)) + ":"));
        result = LOG_MESA.matcher(result).replaceAll(match ->
                Matcher.quoteReplacement(describe(match.group(1), match.group(2)) + "(" + match.group(3) + "):"));
        return LOG_NVIDIA.matcher(result).replaceAll(match ->
                Matcher.quoteReplacement(describe(match.group(1), match.group(2)) + " : "));
    }

    /** The file and the line, an unknown file index stays as the compiler printed it */
    private String describe(final String index, final String line) {
        final var fileIndex = Integer.parseInt(index);
        return fileIndex < files.size() ? files.get(fileIndex) + ":" + line : index + ":" + line;
    }
}
