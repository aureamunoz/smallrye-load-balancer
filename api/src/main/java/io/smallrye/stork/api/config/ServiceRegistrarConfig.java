package io.smallrye.stork.api.config;

public interface ServiceRegistrarConfig extends ServiceDiscoveryConfig {
    default String name() {
        return "";
    }
}
