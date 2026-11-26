package io.github.trimax.venta.studio.logging;

import java.util.function.Consumer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import io.github.trimax.venta.studio.model.LogLine;
import lombok.NonNull;

/**
 * Gives the lines of the log of the engine to the console of the studio.
 */
public final class ConsoleAppender extends AbstractAppender {
    private final Consumer<LogLine> consumer;

    private ConsoleAppender(final String name, final Consumer<LogLine> consumer) {
        super(name, null, null, true, Property.EMPTY_ARRAY);
        this.consumer = consumer;
    }

    /**
     * Starts giving the lines of the log to the consumer. The consumer is called on the thread that wrote the line.
     */
    public static void install(@NonNull final Consumer<LogLine> consumer) {
        final var context = LoggerContext.getContext(false);
        final var configuration = context.getConfiguration();

        final var appender = new ConsoleAppender("StudioConsole", consumer);
        appender.start();
        configuration.addAppender(appender);
        configuration.getRootLogger().addAppender(appender, Level.ALL, null);
        context.updateLoggers();
    }

    @Override
    public void append(final LogEvent event) {
        final var message = event.getMessage().getFormattedMessage();
        final var thrown = event.getThrown();

        consumer.accept(new LogLine(event.getLevel().name(), thrown == null ? message : message + " (" + thrown + ")", event.getTimeMillis()));
    }
}
