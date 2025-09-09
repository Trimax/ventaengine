package io.github.trimax.packer.model;

import org.apache.commons.cli.CommandLine;

import io.github.trimax.packer.enums.PackerOption;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Arguments {
    CommandLine commandLine;

    public String get(final PackerOption option) {
        return commandLine.getOptionValue(option.getCommandLong(), (String) null);
    }
}
