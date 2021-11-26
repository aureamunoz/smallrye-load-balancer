package io.smallrye.stork;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class Metadata<T extends Enum<T>> {

    private final EnumMap<T, Object> metatada;

    private Metadata(Map<T, Object> metatada) {
        this.metatada = new EnumMap<T, Object>(metatada);

    }

    public static Metadata of(Map<?, Object> metatada) {
        return new Metadata(metatada);
    }

    public Map<T, Object> getMetadata() {
        return metatada;
    }



}
