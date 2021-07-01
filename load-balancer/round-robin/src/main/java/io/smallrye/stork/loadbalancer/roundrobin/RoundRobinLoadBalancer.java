package io.smallrye.stork.loadbalancer.roundrobin;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.smallrye.stork.LoadBalancer;
import io.smallrye.stork.ServiceInstance;

public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger index = new AtomicInteger();

    @Override
    public ServiceInstance selectServiceInstance(List<ServiceInstance> serviceInstances) {
        serviceInstances.sort(Comparator.comparingLong(ServiceInstance::getId));
        return select(serviceInstances);
    }

    private ServiceInstance select(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return null;
        }

        return instances.get(index.getAndIncrement() % instances.size());
    }
}
