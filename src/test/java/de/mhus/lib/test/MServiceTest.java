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

public class MServiceTest  extends TestCase {

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

    @Test void testWithServiceFactory() {
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
