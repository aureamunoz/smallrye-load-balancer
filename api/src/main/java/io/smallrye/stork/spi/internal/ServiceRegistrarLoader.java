package io.smallrye.stork.spi.internal;

import io.smallrye.stork.api.ServiceRegistrar;
import io.smallrye.stork.api.config.ServiceRegistrarConfig;
import io.smallrye.stork.spi.ElementWithType;
import io.smallrye.stork.spi.StorkInfrastructure;

/**
 * Used by stork internals to generate service loader for ServiceRegistrarProvider.
 */
public interface ServiceRegistrarLoader extends ElementWithType {

    /**
     * Creates a new {@link ServiceRegistrar} instance.
     *
     * @param config the service registrar configuration, must not be {@code null}
     * @param storkInfrastructure the stork infrastructure, must not be {@code null}
     * @return the new {@link ServiceRegistrar}
     */
    ServiceRegistrar createServiceRegistrar(ServiceRegistrarConfig config,
            StorkInfrastructure storkInfrastructure);
}
