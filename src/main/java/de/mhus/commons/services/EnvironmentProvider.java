package de.mhus.commons.services;

import de.mhus.commons.annotations.service.DefaultImplementation;

@DefaultImplementation(DefaultEnvironmentProvider.class)
public interface EnvironmentProvider extends IService {

    long getEnv(Class<?> owner, String name, long def);
    int getEnv(Class<?> owner, String name, int def);
    String getEnv(Class<?> owner, String name, String def);

}
