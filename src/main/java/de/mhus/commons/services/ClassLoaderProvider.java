package de.mhus.commons.services;

public class ClassLoaderProvider implements IService {

    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
