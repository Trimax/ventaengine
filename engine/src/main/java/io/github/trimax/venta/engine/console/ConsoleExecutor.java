package io.github.trimax.venta.engine.console;

import java.util.Optional;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleExecutor {
    private final ConsoleQueue queue;

    public void execute() {
        while (queue.hasCommands())
            Optional.ofNullable(queue.poll()).ifPresent(this::execute);
    }

    private void execute(final ConsoleQueue.Command command) {
        log.info("Executing command: {}. Comment: {}. Blank: {}", command.value(), command.isComment(), command.isBlank());
    }
}
