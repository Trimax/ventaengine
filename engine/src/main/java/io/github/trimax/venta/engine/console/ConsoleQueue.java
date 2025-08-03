package io.github.trimax.venta.engine.console;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    public record Command(String value, Command parent) {
        public Command(final String value) {
            this(value, null);
        }

        public boolean isComment() {
            return StringUtils.trimToEmpty(value).startsWith("#");
        }

        public boolean isBlank() {
            return StringUtils.isBlank(value);
        }

        public CommandArgument asArgument() {
            return new CommandArgument(StringUtils.trimToNull(value));
        }

        public String getCommand() {
            return StringUtils.substringBefore(StringUtils.trim(value), " ");
        }

        public Command getSubcommand() {
            return new Command(StringUtils.substringAfter(StringUtils.trim(value), " "), this);
        }

        public String getFullPath() {
            if (parent == null)
                return getCommand();

            final var command = getCommand();
            if (StringUtils.isBlank(command))
                return parent().getFullPath();

            return parent.getFullPath() + " " + command;
        }
    }

    public record CommandArgument(String value) {
        public boolean isBlank() {
            return StringUtils.isBlank(value);
        }

        public boolean asBoolean() {
            return ParsingUtil.asBoolean(value);
        }

        public int asInteger() {
            return ParsingUtil.asInteger(value);
        }

        public float asFloat() {
            return ParsingUtil.asFloat(value);
        }

        public Vector2f asVector2f() {
            return ParsingUtil.asVector2f(value);
        }

        public Vector3f asVector3f() {
            return ParsingUtil.asVector3f(value);
        }
    }
}