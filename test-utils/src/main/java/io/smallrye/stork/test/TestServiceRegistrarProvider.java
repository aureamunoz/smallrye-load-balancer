package io.smallrye.stork.test;

import java.util.ArrayList;
import java.util.List;

import io.smallrye.mutiny.Uni;
import io.smallrye.stork.api.Metadata;
import io.smallrye.stork.api.MetadataKey;
import io.smallrye.stork.api.ServiceRegistrar;
import io.smallrye.stork.api.config.ServiceDiscoveryAttribute;
import io.smallrye.stork.api.config.ServiceRegistrarType;
import io.smallrye.stork.spi.ServiceRegistrarProvider;
import io.smallrye.stork.spi.StorkInfrastructure;

@ServiceDiscoveryAttribute(name = "one", description = "no description")
@ServiceDiscoveryAttribute(name = "two", description = "no description")
@ServiceRegistrarType(value = TestServiceRegistrarProvider.TYPE)
public class TestServiceRegistrarProvider
        implements ServiceRegistrarProvider<TestSrRegistrarConfiguration> {

    private static final List<Registration> registrations = new ArrayList<>();
    public static final String TYPE = "test-sr";

    public static void clear() {
        registrations.clear();
    }

    public static List<Registration> getRegistrations() {
        return registrations;
    }

    @Override
    public ServiceRegistrar createServiceRegistrar(TestSrRegistrarConfiguration config,
            String serviceRegistrarName, StorkInfrastructure infrastructure) {
        return new TestServiceRegistrar(config, serviceRegistrarName);
    }

    public static class Registration {
        public final String serviceRegistrarName;
        public final TestSrRegistrarConfiguration config;
        public final Metadata<? extends MetadataKey> metadata;
        public final String ipAddress;
        public final int port;
        public final String serviceName;

        public Registration(String serviceRegistrarName, TestSrRegistrarConfiguration config,
                Metadata<? extends MetadataKey> metadata,
                String serviceName, String ipAddress, int port) {
            this.serviceRegistrarName = serviceRegistrarName;
            this.config = config;
            this.metadata = metadata;
            this.ipAddress = ipAddress;
            this.serviceName = serviceName;
            this.port = port;
        }
    }

    public static class TestServiceRegistrar implements ServiceRegistrar {

        TestSrRegistrarConfiguration config;
        String serviceRegistrarName;

        public TestServiceRegistrar(TestSrRegistrarConfiguration config, String serviceRegistrarName) {
            this.config = config;
            this.serviceRegistrarName = serviceRegistrarName;
        }

        @Override
        public Uni<Void> registerServiceInstance(String serviceName, Metadata<? extends MetadataKey> metadata,
                String ipAddress, int port) {
            registrations.add(new Registration(serviceRegistrarName, config, metadata, serviceName, ipAddress, port));
            return Uni.createFrom().voidItem();
        }
    }

    public enum TestMetadata implements MetadataKey {
        FIRST("pierwszy");

        private final String name;

        TestMetadata(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
