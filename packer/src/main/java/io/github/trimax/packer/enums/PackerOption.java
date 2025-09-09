package io.github.trimax.packer.enums;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import lombok.AllArgsConstructor;
import lombok.Getter;
import one.util.streamex.StreamEx;

@Getter
@AllArgsConstructor
public enum PackerOption {
    Red(true, "r", "red", "red channel source (format: file:sourceChannel)"),
    Green(true, "g", "green", "red channel source (format: file:sourceChannel)"),
    Blue(true, "b", "blue", "red channel source (format: file:sourceChannel)"),
    Alpha(true, "a", "alpha", "red channel source (format: file:sourceChannel)"),
    RedDescription(false, "rd", "red-description", "red channel description"),
    GreenDescription(false, "gd", "green-description", "green channel description"),
    BlueDescription(false, "bd", "blue-description", "blue channel description"),
    AlphaDescription(false, "ad", "alpha-description", "alpha channel description"),
    Output(true, "o", "output", "output file path");

    private final boolean required;
    private final String commandShort;
    private final String commandLong;
    private final String commandDescription;

    private Option getOption() {
        final var option = new Option(commandShort, commandLong, true, commandDescription);
        option.setRequired(required);

        return option;
    }

    public static Options getOptions() {
        final var options = new Options();
        StreamEx.of(values()).map(PackerOption::getOption).forEach(options::addOption);

        return options;
    }
}
