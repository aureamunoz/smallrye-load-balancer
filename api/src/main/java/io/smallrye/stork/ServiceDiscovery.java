package io.smallrye.stork;

import java.util.List;

import io.smallrye.mutiny.Uni;

/**
 * Works with a single service. Provides a stream of all available service instances for a given service.
 *
 * <br>
 * <b>Must be non-blocking</b>
 */
public interface ServiceDiscovery {
    /**
     *
     * @return all `ServiceInstance`'s for the service
     */
    Uni<List<ServiceInstance>> getServiceInstances();
}
