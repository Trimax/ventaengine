package io.github.trimax.venta.engine.console;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.console.ConsoleCommandQueue.Command;

final class ConsoleCommandQueueTest {
    @Test
    void testCommandAndSubcommand() {
        final var command = new Command("scene create forest");

        assertEquals("scene", command.getCommand(), "Command");
        assertEquals("create", command.getSubcommand().getCommand(), "Subcommand");
        assertEquals("forest", command.getSubcommand().getSubcommand().asArgument().value(), "Argument");
    }

    @Test
    void testExtraSpacesAreIgnored() {
        final var subcommand = new Command("  scene   create   forest  ").getSubcommand();

        assertEquals("create", subcommand.getCommand(), "Subcommand after several spaces");
        assertEquals("forest", subcommand.getSubcommand().asArgument().value(), "Argument after several spaces");
    }

    @Test
    void testFullPath() {
        final var argument = new Command("scene create forest").getSubcommand().getSubcommand();

        assertEquals("scene create forest", argument.getFullPath(), "Full path includes all levels");
        assertEquals("scene create", argument.parent().getFullPath(), "Full path of the parent");
    }

    @Test
    void testFullPathOfMissingSubcommand() {
        final var missing = new Command("scene").getSubcommand();

        assertTrue(missing.asArgument().isBlank(), "Missing subcommand is blank");
        assertEquals("scene", missing.getFullPath(), "Missing subcommand doesn't extend the path");
    }

    @Test
    void testBlankAndComments() {
        assertTrue(new Command("   ").isBlank(), "Spaces only");
        assertTrue(new Command(null).isBlank(), "No value");
        assertTrue(new Command("  # comment").isComment(), "Comment after spaces");
        assertFalse(new Command("scene list").isComment(), "Regular command");
    }

    @Test
    void testArguments() {
        assertTrue(new Command("true").asArgument().asBoolean(), "Boolean");
        assertEquals(5, new Command(" 5 ").asArgument().asInteger(), "Integer with spaces");
        assertEquals(1.5f, new Command("1.5").asArgument().asFloat(), "Float");
        assertEquals(new Vector3f(1, 2, 3), new Command("[1 2 3]").asArgument().asVector3f(), "Vector");
        assertNull(new Command("   ").asArgument().value(), "Blank argument has no value");
    }

    @Test
    void testQueueOrder() throws Exception {
        final var constructor = ConsoleCommandQueue.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final var queue = constructor.newInstance();

        assertFalse(queue.hasCommands(), "New queue is empty");
        queue.add(new Command("first"));
        queue.add(new Command("second"));

        assertEquals("first", queue.poll().value(), "Commands are executed in order");
        assertEquals("second", queue.poll().value(), "Commands are executed in order");
        assertNull(queue.poll(), "Empty queue returns nothing");
    }
}
