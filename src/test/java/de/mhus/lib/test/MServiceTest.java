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
package de.mhus.lib.test;

import de.mhus.commons.annotations.service.DefaultImplementation;
import de.mhus.commons.annotations.service.DefaultImplementationNull;
import de.mhus.commons.annotations.service.Factory;
import de.mhus.commons.annotations.service.ServiceFactory;
import de.mhus.commons.services.IService;
import de.mhus.commons.services.MService;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MServiceTest extends TestCase {

    @Test
    public void testNewInstance() {
        ServiceNewInstance service = MService.getService(ServiceNewInstance.class);
        assertNotNull(service);
    }

    @Test
    public void testWithDefaultInstance() {
        ServiceWithDefaultImplementation service = MService.getService(ServiceWithDefaultImplementation.class);
        assertNotNull(service);
        assertTrue(service instanceof ServiceWithDefaultImplementationDefault);
    }

    @Test
    public void testWithDefaultImplementationNull() {
        ServiceWithDefaultImplementationNull service = MService.getService(ServiceWithDefaultImplementationNull.class);
        assertNull(service);
    }

    @Test
    void testWithServiceFactory() {
        ServiceWithServiceFactory service = MService.getService(ServiceWithServiceFactory.class);
        assertNotNull(service);
        assertTrue(service instanceof ServiceWithServiceFactoryImpl);
    }

    public static class ServiceNewInstance implements IService {
    }

    @DefaultImplementation(ServiceWithDefaultImplementationDefault.class)
    public interface ServiceWithDefaultImplementation extends IService {
    }

    public static class ServiceWithDefaultImplementationDefault implements ServiceWithDefaultImplementation {
    }

    @DefaultImplementationNull()
    public interface ServiceWithDefaultImplementationNull extends IService {
    }

    @ServiceFactory(ServiceWithServiceFactoryServiceFactory.class)
    public interface ServiceWithServiceFactory extends IService {
    }

    public static class ServiceWithServiceFactoryServiceFactory implements Factory<ServiceWithServiceFactory> {

        @Override
        public ServiceWithServiceFactory create(Class<ServiceWithServiceFactory> clazz) {
            return new ServiceWithServiceFactoryImpl();
        }
    }

    public static class ServiceWithServiceFactoryImpl implements ServiceWithServiceFactory {
    }

}
