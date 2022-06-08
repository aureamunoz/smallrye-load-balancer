package io.smallrye.stork.spi.config;

import java.util.Map;

import io.smallrye.stork.api.config.ServiceRegistrarConfig;

public class SimpleRegistrarConfig implements ServiceRegistrarConfig {
    private final String type;
    private final String name;
    private final Map<String, String> parameters;

    public SimpleRegistrarConfig(String type, String name, Map<String, String> parameters) {
        this.type = type;
        this.name = name;
        this.parameters = parameters;
    }

    public String type() {
        return type;
    }

    public Map<String, String> parameters() {
        return parameters;
    }

    public String name() {
        return name;
    }

}
