package io.smallrye.stork.servicediscovery.eureka;

import jakarta.enterprise.context.ApplicationScoped;

import io.smallrye.stork.api.ServiceDiscovery;
import io.smallrye.stork.api.config.ServiceConfig;
import io.smallrye.stork.api.config.ServiceDiscoveryAttribute;
import io.smallrye.stork.api.config.ServiceDiscoveryType;
import io.smallrye.stork.impl.CachingServiceDiscovery;
import io.smallrye.stork.spi.ServiceDiscoveryProvider;
import io.smallrye.stork.spi.StorkInfrastructure;

/**
 * Service Discovery provider creating instances of {@link EurekaServiceDiscovery}.
 */
@ServiceDiscoveryType("eureka")
@ServiceDiscoveryAttribute(name = "eureka-host", description = "The Eureka server host.", required = true)
@ServiceDiscoveryAttribute(name = "eureka-port", description = "The Eureka server port.", defaultValue = "8761")
@ServiceDiscoveryAttribute(name = "eureka-context-path", description = "The Eureka server root context path.", defaultValue = "/")
@ServiceDiscoveryAttribute(name = "application", description = "The Eureka application Id; if not defined Stork service name will be used")
@ServiceDiscoveryAttribute(name = "eureka-trust-all", description = "Enable/Disable the TLS certificate verification", defaultValue = "false")
@ServiceDiscoveryAttribute(name = "eureka-tls", description = "Use TLS to connect to the Eureka server", defaultValue = "false")
@ServiceDiscoveryAttribute(name = "instance", description = "The Eureka application instance Id")
@ServiceDiscoveryAttribute(name = "refresh-period", description = "Service discovery cache refresh period.", defaultValue = CachingServiceDiscovery.DEFAULT_REFRESH_INTERVAL)
@ServiceDiscoveryAttribute(name = "secure", description = "Whether is should select the secured endpoint of the retrieved services.", defaultValue = "false")
@ApplicationScoped
public class EurekaServiceDiscoveryProvider implements ServiceDiscoveryProvider<EurekaConfiguration> {

    @Override
    public ServiceDiscovery createServiceDiscovery(EurekaConfiguration config, String serviceName,
            ServiceConfig serviceConfig, StorkInfrastructure infrastructure) {
        return new EurekaServiceDiscovery(config, serviceName, infrastructure);
    }
}
