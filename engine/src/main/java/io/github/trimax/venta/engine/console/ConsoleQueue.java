package io.github.trimax.venta.engine.console;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleQueue {
    private final BlockingQueue<Command> queue = new LinkedBlockingQueue<>();

    @SneakyThrows
    public void add(@NonNull final Command command) {
        queue.put(command);
    }

    public Command poll() {
        return queue.poll();
    }

    public boolean hasCommands() {
        return !queue.isEmpty();
    }

    public record Command(String value) {}
}