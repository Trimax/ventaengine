package io.github.trimax.packer;

import io.github.trimax.packer.enums.PackerOption;
import io.github.trimax.packer.model.Arguments;
import io.github.trimax.packer.util.ParsingUtil;
import one.util.streamex.StreamEx;

public final class Packer {
    public static void main(final String[] args) {
        try {
            pack(ParsingUtil.parse(args));
        } catch (final RuntimeException ignored) {}
    }

    private static void pack(final Arguments arguments) {
        StreamEx.of(PackerOption.values())
                .forEach(option -> System.out.println(option.getCommandLong() + ": " + arguments.get(option)));
    }
}
