package de.mhus.commons.annotations.service;

import de.mhus.commons.services.IService;

public interface Factory<T extends IService> {
    T create(Class<T> clazz);
}
