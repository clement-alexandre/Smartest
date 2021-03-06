package fr.unice.polytech.pnsinnov.smartest.cli;

import fr.unice.polytech.pnsinnov.smartest.Context;
import fr.unice.polytech.pnsinnov.smartest.configuration.DefaultConfigReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandLineParserTest {
    private CommandLineParser commandLineParser;
    private Map<String, Boolean> commandDone;
    private ByteArrayOutputStream outByteArray;
    private MockedListTests mockedListTests;
    private MockedTest mockedTest;
    private MockedCommit mockedCommit;

    @BeforeEach
    void setUp() {
        ByteArrayInputStream in = new ByteArrayInputStream("\n".getBytes());
        outByteArray = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outByteArray);
        PrintStream err = new PrintStream(new ByteArrayOutputStream());
        Context context = new Context.ContextBuilder().withInputStream(in).withOutStream(out).withErrStream(err)
                .build();
        commandLineParser = new CommandLineParser(new DefaultConfigReader(), context);

        commandDone = new HashMap<>();
        commandDone.put("list-tests", false);
        commandDone.put("test", false);
        commandDone.put("commit", false);

        mockedListTests = new MockedListTests();
        mockedTest = new MockedTest();
        mockedCommit = new MockedCommit();

        commandLineParser.addSubCommand("list-tests", mockedListTests);
        commandLineParser.addSubCommand("test", mockedTest);
        commandLineParser.addSubCommand("commit", mockedCommit);
    }

    @Test
    void noArgument() {
        commandLineParser.parse();
        assertEquals(Paths.get("config.smt"), commandLineParser.getConfigPath());
        assertEquals(false, commandDone.get("list-tests"));
        assertEquals(false, commandDone.get("test"));
        assertEquals(false, commandDone.get("commit"));
    }

    @Test
    void changeConfigPath() {
        commandLineParser.parse("--config-path", "config.json");
        assertEquals(Paths.get("config.json"), commandLineParser.getConfigPath());
    }

    @Test
    void listTestNoScope() {
        mockedListTests.expected = "Class";
        commandLineParser.parse("list-tests");
        assertEquals(true, commandDone.get("list-tests"));
        assertEquals(false, commandDone.get("test"));
        assertEquals(false, commandDone.get("commit"));
    }

    @Test
    void listTestsMethodScope() {
        mockedListTests.expected = "Method";
        commandLineParser.parse("list-tests", "-s", "Method");
        assertEquals(true, commandDone.get("list-tests"));
        assertEquals(false, commandDone.get("test"));
        assertEquals(false, commandDone.get("commit"));
    }

    @Test
    void testNoScope() {
        mockedTest.expected = "Class";
        commandLineParser.parse("test");
        assertEquals(false, commandDone.get("list-tests"));
        assertEquals(true, commandDone.get("test"));
        assertEquals(false, commandDone.get("commit"));
    }

    @Test
    void testMethodScope() {
        mockedTest.expected = "Method";
        commandLineParser.parse("test", "-s", "Method");
        assertEquals(false, commandDone.get("list-tests"));
        assertEquals(true, commandDone.get("test"));
        assertEquals(false, commandDone.get("commit"));
    }

    @Test
    void commitNoScope() {
        mockedCommit.expected = "Class";
        commandLineParser.parse("commit", "-m", "Initial commit");
        assertEquals(false, commandDone.get("list-tests"));
        assertEquals(false, commandDone.get("test"));
        assertEquals(true, commandDone.get("commit"));
    }

    @Test
    void commitMethodScope() {
        mockedCommit.expected = "Method";
        commandLineParser.parse("commit", "-m", "Initial commit", "-s", "Method");
        assertEquals(false, commandDone.get("list-tests"));
        assertEquals(false, commandDone.get("test"));
        assertEquals(true, commandDone.get("commit"));
    }

    @Test
    void commitWithoutMessage() {
        commandLineParser.parse("commit", "-s", "Method");
    }

    @CommandLine.Command(name = "commit", description = "Mock the commit feature")
    private class MockedCommit extends Command {
        @CommandLine.Option(names = {"-m", "--message"}, required = true, description = "Put an useless message here")
        private String message;

        @CommandLine.Option(names = {"-s", "--scope"}, description = "Module, Class, ...")
        private String scope = "Class";
        private String expected;

        @Override
        public void run() {
            commandDone.put("commit", true);
            assertEquals("Initial commit", message);
            assertEquals(expected, scope);
        }
    }

    @CommandLine.Command(name = "test", description = "Mock the test feature")
    private class MockedTest extends Command {
        @CommandLine.Option(names = {"-s", "--scope"}, description = "Module, Class, ...")
        private String scope = "Class";
        private String expected;

        @Override
        public void run() {
            commandDone.put("test", true);
            assertEquals(expected, scope);
        }
    }

    @CommandLine.Command(name = "list-tests", description = "Mock the list-tests feature")
    private class MockedListTests extends Command {
        @CommandLine.Option(names = {"-s", "--scope"}, description = "Module, Class, ...")
        private String scope = "Class";
        private String expected;

        @Override
        public void run() {
            commandDone.put("list-tests", true);
            assertEquals(expected, scope);
        }
    }
}