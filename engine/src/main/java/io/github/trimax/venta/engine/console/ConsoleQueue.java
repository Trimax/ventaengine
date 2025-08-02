package io.github.trimax.venta.engine.console;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.utils.ParsingUtil;
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

    public record Command(String value) {
        public boolean isComment() {
            return StringUtils.trimToEmpty(value).startsWith("#");
        }

        public boolean isBlank() {
            return StringUtils.isBlank(value);
        }

        public boolean hasArguments() {
            return StringUtils.isNotBlank(getArguments());
        }

        public String getTrimmed() {
            return StringUtils.trimToNull(value);
        }

        public String getCommand() {
            return StringUtils.substringBefore(StringUtils.trim(value), " ");
        }

        public Command getSubcommand() {
            return new Command(getArguments());
        }

        public String getArguments() {
            return StringUtils.substringAfter(StringUtils.trim(value), " ");
        }
    }

    public record CommandArgument(String raw) {
        public boolean asBoolean() {
            return ParsingUtil.asBoolean(raw);
        }

        public int asInteger() {
            return ParsingUtil.asInteger(raw);
        }

        public float asFloat() {
            return ParsingUtil.asFloat(raw);
        }

        public Vector2f asVector2f() {
            return ParsingUtil.asVector2f(raw);
        }

        public Vector3f asVector3f() {
            return ParsingUtil.asVector3f(raw);
        }
    }
}