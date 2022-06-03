package io.smallrye.stork.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.smallrye.stork.api.config.LoadBalancerConfig;
import io.smallrye.stork.api.config.ServiceConfig;
import io.smallrye.stork.api.config.ServiceDiscoveryConfig;
import io.smallrye.stork.api.config.ServiceRegistrarConfig;
import io.smallrye.stork.spi.config.ConfigProvider;

/**
 * Stork config provider for tests, allows easily configuring stuff programmatically
 */
public class TestConfigProvider implements ConfigProvider {
    private static final List<ServiceConfig> configs = new ArrayList<>();
    private static final List<ServiceRegistrarConfig> registrarConfigs = new ArrayList<>();

    private static int priority = Integer.MAX_VALUE;

    public static void setPriority(int priority) {
        TestConfigProvider.priority = priority;
    }

    public static int getPriority() {
        return priority;
    }

    @Deprecated
    public static void addServiceConfig(String name, String loadBalancer, String serviceDiscovery,
            Map<String, String> loadBalancerParams, Map<String, String> serviceDiscoveryParams, boolean secure) {
        if (secure) {
            serviceDiscoveryParams.put("secure", "true");
        }
        addServiceConfig(name, loadBalancer, serviceDiscovery, loadBalancerParams, serviceDiscoveryParams);
    }

    public static void addServiceConfig(String name, String loadBalancer, String serviceDiscovery,
            Map<String, String> loadBalancerParams, Map<String, String> serviceDiscoveryParams) {
        configs.add(new ServiceConfig() {
            @Override
            public String serviceName() {
                return name;
            }

            @Override
            public LoadBalancerConfig loadBalancer() {
                return loadBalancer == null ? null : new LoadBalancerConfig() {
                    @Override
                    public String type() {
                        return loadBalancer;
                    }

                    @Override
                    public Map<String, String> parameters() {
                        return Objects.requireNonNullElse(loadBalancerParams, Collections.emptyMap());
                    }
                };
            }

            @Override
            public ServiceDiscoveryConfig serviceDiscovery() {
                return new ServiceDiscoveryConfig() {
                    @Override
                    public String type() {
                        return serviceDiscovery;
                    }

                    @Override
                    public Map<String, String> parameters() {
                        return Objects.requireNonNullElse(serviceDiscoveryParams, Collections.emptyMap());
                    }
                };
            }

            @Override
            public ServiceRegistrarConfig serviceRegistrar() {
                return null;
            }
        });
    }

    public static void addServiceRegistrarConfig(String registrarName, String registrarType, Map<String, String> parameters) {
        registrarConfigs.add(new ServiceRegistrarConfig() {
            public String name() {
                return registrarName;
            }

            public String type() {
                return registrarType;
            }

            public Map<String, String> parameters() {
                return parameters;
            }
        });
    }

    public static void clear() {
        configs.clear();
        registrarConfigs.clear();
    }

    @Override
    public List<ServiceConfig> getConfigs() {
        return configs;
    }

    @Override
    public List<ServiceRegistrarConfig> getRegistrarConfigs() {
        return registrarConfigs;
    }

    @Override
    public int priority() {
        return priority;
    }
}
