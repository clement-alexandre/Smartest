package fr.unice.polytech.pnsinnov.smartest.plugin.language.diff;

import java.util.Objects;

public class TestImplementation implements fr.smartest.plugin.Test {


    private Priority priority;
    private String Identifier;

    public TestImplementation(String identifier) {
        this.priority = Priority.MEDIUM;
        Identifier = identifier;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public String getIdentifier() {
        return Identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestImplementation that = (TestImplementation) o;
        return priority == that.priority &&
                Objects.equals(Identifier, that.Identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, Identifier);
    }

    @Override
    public String toString() {
        return "TestImplementation{priority=" + priority + ", Identifier='" + Identifier + "\'}";
    }
}
