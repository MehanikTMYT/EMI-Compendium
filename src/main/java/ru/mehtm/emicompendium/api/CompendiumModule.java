package ru.mehtm.emicompendium.api;

import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiRegistry;

public interface CompendiumModule {
    String id();

    /** Early EMI init (ingredient serializers, registry adapters). */
    default void initialize(EmiInitRegistry registry) {
    }

    void register(EmiRegistry registry);
}
