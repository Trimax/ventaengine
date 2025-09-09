package io.github.trimax.packer.util;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import io.github.trimax.packer.enums.PackerOption;
import io.github.trimax.packer.model.Arguments;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ParsingUtil {
    @SneakyThrows
    public Arguments parse(final String[] args) {
        final var options = PackerOption.getOptions();

        try {
            return new Arguments(new DefaultParser().parse(options, args));
        } catch (final ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter.builder()
                    .setShowSince(false)
                    .get()
                    .printHelp("packer", "Texture packer. Merges channels from multiple sources into one texture", options, null, true);
        }

        throw new RuntimeException("Failed to parse command-ling arguments");
    }
}
