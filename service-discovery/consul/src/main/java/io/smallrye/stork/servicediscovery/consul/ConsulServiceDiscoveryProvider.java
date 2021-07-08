package io.smallrye.stork.servicediscovery.consul;

import io.smallrye.stork.ServiceDiscovery;
import io.smallrye.stork.config.ServiceDiscoveryConfig;
import io.smallrye.stork.spi.ServiceDiscoveryProvider;
import io.vertx.core.Vertx;

public class ConsulServiceDiscoveryProvider implements ServiceDiscoveryProvider {

    @Override
    public ServiceDiscovery createServiceDiscovery(ServiceDiscoveryConfig config, String serviceName) {
        return new ConsulServiceDiscovery(serviceName, config, Vertx.vertx()); // TODO: ability to provide a vertx instance

    }

    @Override
    public String type() {
        return "consul";
    }
}
