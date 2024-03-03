package de.mhus.commons.util;

import de.mhus.commons.annotations.service.DefaultImplementation;
import de.mhus.commons.services.IService;

import java.util.Locale;

@DefaultImplementation(DefaultMessageSource.class)
public interface MessageSource extends IService {
    String getMessage(String message, Object[] args, String def, Locale locale);
}
