/**
 * Copyright (C) 2002 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.commons.services;

import de.mhus.commons.tools.MObject;
import de.mhus.commons.annotations.service.DefaultImplementation;
import de.mhus.commons.annotations.service.DefaultImplementationNull;
import de.mhus.commons.annotations.service.ServiceFactory;
import de.mhus.commons.errors.RC;
import de.mhus.commons.errors.MRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MService {

    private static Map<Class<? extends IService>, IService> services = new HashMap<>();

    public synchronized static <T extends IService> T getService(Class<T> clazz) {
        T service = (T) services.get(clazz);
        if (service == null) {
            try {
                final var nullAnno = clazz.getAnnotation(DefaultImplementationNull.class);
                if (nullAnno != null) {
                    return null;
                }
                final var factoryAnno = clazz.getAnnotation(ServiceFactory.class);
                if (factoryAnno != null) {
                    service = (T) MObject.newInstance(factoryAnno.value()).create(clazz);
                }
                if (service == null) {
                    final var defaultAnno = clazz.getAnnotation(DefaultImplementation.class);
                    if (defaultAnno != null) {
                        service = (T) defaultAnno.value().newInstance();
                    } else {
                        service = MObject.newInstance(clazz);
                    }
                }
                LOGGER.debug("Create service {} with {}", clazz, service.getClass());
                services.put(clazz, service);
            } catch (Exception e) {
                LOGGER.error("Can't create service {}", clazz, e);
                throw new MRuntimeException(RC.STATUS.INTERNAL_ERROR, "Can't create service", clazz, e);
            }
        }
        return service;
    }

    public static void registerService(Class<? extends IService> clazz, IService service) {
        services.put(clazz, service);
    }

}
