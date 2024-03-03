package de.mhus.commons.services;

import de.mhus.commons.MSystem;
import de.mhus.commons.annotations.service.DefaultImplementation;
import de.mhus.commons.annotations.service.DefaultImplementationNull;
import de.mhus.commons.annotations.service.ServiceFactory;
import de.mhus.commons.basics.RC;
import de.mhus.commons.errors.MRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MService {

    private static Map<Class<? extends IService>,IService> services = new HashMap<>();

    public synchronized static <T extends IService> T getService(Class<T> clazz) {
        T service = (T)services.get(clazz);
        if (service == null) {
            try {
                final var nullAnno = clazz.getAnnotation(DefaultImplementationNull.class);
                if (nullAnno != null) {
                    return null;
                }
                final var factoryAnno = clazz.getAnnotation(ServiceFactory.class);
                if (factoryAnno != null) {
                    service = MSystem.newInstance(factoryAnno.value()).create(clazz);
                }
                final var defaultAnno = clazz.getAnnotation(DefaultImplementation.class);
                if (defaultAnno != null) {
                    service = (T)defaultAnno.value().newInstance();
                } else {
                    service = MSystem.newInstance(clazz);
                }
                LOGGER.debug("Create service {} with {}", clazz, service.getClass());
                services.put(clazz, service);
            } catch (Exception e) {
                LOGGER.error("Can't create service {}", clazz, e);
                throw new MRuntimeException(RC.STATUS.INTERNAL_ERROR,"Can't create service", clazz, e);
            }
        }
        return service;
    }

    public static void registerService(Class<? extends IService> clazz, IService service) {
        services.put(clazz, service);
    }


}
