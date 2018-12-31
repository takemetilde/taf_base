package com.taf.auto.jira;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents the IT Teams in JIRA.
 *
 * @author AF04261
 */
public class ITTeams implements Comparable<ITTeams> {
    public static final int NO_TEAM = -1;

    public final int id;
    public final String name;

    public ITTeams() {
        this(NO_TEAM, "Undefined");
    }

    public ITTeams(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean peekUndefined() {
        return id == NO_TEAM;
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull ITTeams o) {
        return name.compareToIgnoreCase(o.name);
    }

    public boolean equals(Object o) {
        if(o instanceof ITTeams) {
            return id == ((ITTeams)o).id;
        }
        return super.equals(o);
    }

    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Deprecated
    public static Optional<ITTeams> peek(int id) {
        return Optional.of(new ITTeams(id, "Fabricated"));
    }
}
