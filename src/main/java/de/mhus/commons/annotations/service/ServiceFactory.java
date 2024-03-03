package de.mhus.commons.annotations.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceFactory {
    Class<? extends Factory> value();
}
