package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class FormatUtilTest {
    @Test
    void testSizeIsInTheClosestUnit() {
        assertEquals("0 B", FormatUtil.size(0L));
        assertEquals("1023 B", FormatUtil.size(1023L));
        assertEquals("1.0 KB", FormatUtil.size(1024L));
        assertEquals("1.5 KB", FormatUtil.size(1536L));
        assertEquals("1024.0 KB", FormatUtil.size(1024L * 1024 - 1));
        assertEquals("2.0 MB", FormatUtil.size(2L * 1024 * 1024));
    }

    @Test
    void testSizeOfAFile(@TempDir final Path directory) throws IOException {
        final var file = Files.write(directory.resolve("data.bin"), new byte[2048]);

        assertEquals("2.0 KB", FormatUtil.size(file));
    }

    @Test
    void testFileThatCanNotBeReadHasNoSize(@TempDir final Path directory) {
        assertEquals("", FormatUtil.size(directory.resolve("missing.bin")));
    }
}
