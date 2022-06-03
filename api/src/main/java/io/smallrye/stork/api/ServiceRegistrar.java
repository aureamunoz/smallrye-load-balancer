package io.smallrye.stork.api;

import io.smallrye.mutiny.Uni;

public interface ServiceRegistrar {

    default Uni<Void> registerServiceInstance(String serviceName, String ipAddress, int port) {
        return registerServiceInstance(serviceName, Metadata.empty(), ipAddress, port);
    }

    Uni<Void> registerServiceInstance(String serviceName, Metadata<? extends MetadataKey> metadata, String ipAddress, int port);
}
