package io.wispforest.alloyforgery.utils;

import io.wispforest.alloyforgery.AlloyForgery;

import java.util.ServiceLoader;

public interface GeneralClientPlatformUtils {
    GeneralClientPlatformUtils INSTANCE = load(GeneralClientPlatformUtils.class);

    private static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));

        AlloyForgery.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);

        return loadedService;
    }
}
