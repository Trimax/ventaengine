package io.github.trimax.packer;

import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import io.github.trimax.packer.enums.PackerOption;
import io.github.trimax.packer.model.Arguments;
import io.github.trimax.packer.model.Image;
import io.github.trimax.packer.model.Source;
import io.github.trimax.packer.util.ImageUtil;
import io.github.trimax.packer.util.ParsingUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Packer {
    public static void main(final String[] args) {
        try {
            pack(ParsingUtil.parse(args));
            log.info("Texture packed successfully");
        } catch (final RuntimeException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }

    private static void pack(final Arguments arguments) {
        final var sourceRed = load(arguments, PackerOption.Red);
        final var sourceGreen = load(arguments, PackerOption.Green);
        final var sourceBlue = load(arguments, PackerOption.Blue);
        final var sourceAlpha = load(arguments, PackerOption.Alpha);

        final var size = getDimension(sourceRed, sourceGreen, sourceBlue, sourceAlpha);
        final var destination = new byte[size.x() * size.y() * 4];

        for (int y = 0; y < size.y(); y++) {
            for (int x = 0; x < size.x(); x++) {
                final var index = (x + y * size.x()) * 4;

                destination[index    ] = sourceRed.getValue(x, y);
                destination[index + 1] = sourceGreen.getValue(x, y);
                destination[index + 2] = sourceBlue.getValue(x, y);
                destination[index + 3] = sourceAlpha.getValue(x, y);
            }
        }

        ImageUtil.write(arguments.get(PackerOption.Output), new Image(size.x(), size.y(), 4, destination));
    }

    private static Source load(final Arguments arguments, final PackerOption option) {
        return Optional.of(option)
                .map(arguments::get)
                .map(Source::of)
                .orElse(null);
    }

    private static Vector2ic getDimension(final Source... sources) {
        if (ArrayUtils.isEmpty(sources))
            throw new RuntimeException("No textures loaded");

        final var width = sources[0].image().width();
        final var height = sources[0].image().width();

        for (final var source : sources)
            if (source.image().width() != width || source.image().height() != height)
                throw new IllegalArgumentException("All textures must have the same dimensions");

        return new Vector2i(width, height);
    }
}
