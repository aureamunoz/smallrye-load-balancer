package io.smallrye.stork.spi;

import io.smallrye.stork.api.ServiceRegistrar;

public interface ServiceRegistrarProvider<T> {
    ServiceRegistrar createServiceRegistrar(T config, String serviceRegistrarName,
            StorkInfrastructure infrastructure);
}
