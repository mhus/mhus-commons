package de.mhus.commons;

public class UniqueId {

    private static long nextId;

    public static long nextUniqueId() {
        return nextId++;
    }
}
