package io.smallrye.stork.servicediscovery.kubernetes;

import static io.smallrye.stork.config.StorkConfigHelper.getOrDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointPort;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.stork.CachingServiceDiscovery;
import io.smallrye.stork.DefaultServiceInstance;
import io.smallrye.stork.Metadata;
import io.smallrye.stork.ServiceInstance;
import io.smallrye.stork.config.ServiceDiscoveryConfig;
import io.smallrye.stork.spi.ServiceInstanceIds;
import io.smallrye.stork.spi.ServiceInstanceUtils;
import io.vertx.core.Vertx;

public class KubernetesServiceDiscovery extends CachingServiceDiscovery {

    public static final String METADATA_NAME = "metadata.name";
    private final KubernetesClient client;
    private final String serviceName;
    private String application;
    private final boolean allNamespaces;
    private final String namespace;
    private final boolean secure;
    private final Vertx vertx;

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesServiceDiscovery.class);

    public KubernetesServiceDiscovery(String serviceName, ServiceDiscoveryConfig config, Vertx vertx, boolean secure) {
        super(config);
        Config base = Config.autoConfigure(null);
        this.serviceName = serviceName;
        Map<String, String> parameters = config.parameters();
        String masterUrl = getOrDefault(config, "k8s-host", base.getMasterUrl());
        this.application = getOrDefault(config, "application", serviceName);
        namespace = getOrDefault(config, "k8s-namespace", base.getNamespace());
        allNamespaces = namespace != null && namespace.equalsIgnoreCase("all");

        Config k8sConfig = new ConfigBuilder(base)
                .withMasterUrl(masterUrl)
                .withNamespace(namespace).build();
        client = new DefaultKubernetesClient(k8sConfig);
        this.vertx = vertx;
        this.secure = secure;
    }

    @Override
    public Uni<List<ServiceInstance>> fetchNewServiceInstances(List<ServiceInstance> previousInstances) {
        Uni<List<Endpoints>> endpointsUni = Uni.createFrom().emitter(
                emitter -> {
                    vertx.executeBlocking(future -> {
                        List<Endpoints> endpoints = new ArrayList<>();
                        if (allNamespaces) {
                            endpoints.addAll(
                                    client.endpoints().inAnyNamespace().withField(METADATA_NAME, application).list()
                                            .getItems());
                        } else {
                            endpoints.addAll(
                                    client.endpoints().inNamespace(namespace).withField(METADATA_NAME, application)
                                            .list()
                                            .getItems());
                        }
                        future.complete(endpoints);
                    }, result -> {
                        if (result.succeeded()) {
                            @SuppressWarnings("unchecked")
                            List<Endpoints> endpoints = (List<Endpoints>) result.result();
                            emitter.complete(endpoints);
                        } else {
                            LOGGER.error("Unable to retrieve the endpoint from the {} service", application,
                                    result.cause());
                            emitter.fail(result.cause());
                        }
                    });
                });
        return endpointsUni.onItem().transform(endpoints -> toStorkServiceInstances(endpoints, previousInstances));
    }

    private List<ServiceInstance> toStorkServiceInstances(List<Endpoints> endpointList,
            List<ServiceInstance> previousInstances) {
        List<ServiceInstance> serviceInstances = new ArrayList<>();
        for (Endpoints endPoints : endpointList) {
            for (EndpointSubset subset : endPoints.getSubsets()) {
                for (EndpointAddress endpointAddress : subset.getAddresses()) {
                    String hostname = endpointAddress.getIp();
                    if (hostname == null) { // should we take the hostName?
                        hostname = endpointAddress.getHostname();
                    }
                    List<EndpointPort> endpointPorts = subset.getPorts();
                    Integer port = 0;
                    if (endpointPorts.size() == 1) {
                        port = endpointPorts.get(0).getPort();
                    }

                    ServiceInstance matching = ServiceInstanceUtils.findMatching(previousInstances, hostname, port);
                    if (matching != null) {
                        serviceInstances.add(matching);
                    } else {
                        Map<String, String> labels = endPoints.getMetadata().getLabels() != null
                                ? endPoints.getMetadata().getLabels()
                                : Collections.emptyMap();
                        //TODO add some useful metadata?
                        Map<KubernetesMetadataKey, Object> metadata = new EnumMap<>(KubernetesMetadataKey.class);
                        metadata.put(META_K8S_SERVICE_ID, hostname);
                        Metadata<KubernetesMetadataKey> k8sMetadata = new Metadata<>(metadata);

                        serviceInstances.add(new DefaultServiceInstance(ServiceInstanceIds.next(), hostname, port, secure,
                                labels, k8sMetadata));
                    }
                }
            }
        }

        return serviceInstances;
    }
}
