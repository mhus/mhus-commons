package de.mhus.commons.services;

public class UniqueId implements IService {

    private static long nextId;

    public long nextUniqueId() {
        return nextId++;
    }
}
