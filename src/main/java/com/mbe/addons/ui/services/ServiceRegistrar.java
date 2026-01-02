package com.mbe.addons.ui.services;

@FunctionalInterface
public interface ServiceRegistrar {
    void register(Class<?> type, Object service);
}
