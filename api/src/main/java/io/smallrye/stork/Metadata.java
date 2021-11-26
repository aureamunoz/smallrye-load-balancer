package io.smallrye.stork;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class Metadata<T extends Enum<T>> {

    private final EnumMap<T, Object> metatada;
    private final Class<T> enumType;

    private Metadata(Class<T> key) {
        this.metatada = new EnumMap<T, Object>(key);
        this.enumType = key;

    }

    public static Metadata of(Class<?> key) {
        return new Metadata(key);
    }

    public Map<T, Object> getMetadata() {
        return metatada;
    }

    public Optional<Object> get(T key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be `null`");
        }
        return Optional.ofNullable(metatada.get(key));
    }

    public void put(T key, Object o) {
        metatada.put(key, o);
    }

}
