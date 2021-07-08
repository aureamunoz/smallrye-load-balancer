package io.smallrye.stork;

import java.util.List;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class Service {

    private final LoadBalancer loadBalancer;
    private final ServiceDiscovery serviceDiscovery;
    private final String serviceName;

    public Service(String serviceName, LoadBalancer loadBalancer, ServiceDiscovery serviceDiscovery) {
        this.loadBalancer = loadBalancer;
        this.serviceDiscovery = serviceDiscovery;
        this.serviceName = serviceName;
    }

    /**
     * Provide a single {@link ServiceInstance}
     *
     * @return a Uni with a ServiceInstance
     */
    public Uni<ServiceInstance> selectServiceInstance() {
        return serviceDiscovery.getServiceInstances()
                .map(loadBalancer::selectServiceInstance);
    }

    /**
     * Provide a collection of {@link ServiceInstance}s
     *
     * @return a Multi - stream of ServiceInstances
     */
    public Uni<List<ServiceInstance>> getServiceInstances() {
        return serviceDiscovery.getServiceInstances();
    }

    /**
     * Get the underlying load balancer
     *
     * @return load balancer
     */
    public LoadBalancer getLoadBalancer() {
        if (loadBalancer == null) {
            throw new IllegalArgumentException("No load balancer for service '" + serviceName + "' defined");
        }
        return loadBalancer;
    }

    /**
     * Get the underlying service discovery
     *
     * @return service discovery
     */
    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }
}
