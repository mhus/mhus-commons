package de.mhus.commons.services;

import de.mhus.commons.tools.MCast;

public class DefaultEnvironmentProvider implements EnvironmentProvider {
    @Override
    public long getEnv(Class<?> owner, String name, long def) {
        return MCast.tolong(System.getenv(toEnvName(owner, name)), def);
    }

    private String toEnvName(Class<?> owner, String name) {
        return owner.getName().toUpperCase().replace('.', '_') + "_" + name.toUpperCase();
    }

    @Override
    public int getEnv(Class<?> owner, String name, int def) {
        return MCast.toint(System.getenv(toEnvName(owner, name)), def);
    }

    @Override
    public String getEnv(Class<?> owner, String name, String def) {
        final var value = System.getenv(toEnvName(owner, name));
        return value != null ? value : def;
    }
}
