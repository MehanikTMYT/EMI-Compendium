package ru.mehanik.emicompendium.api;

import dev.emi.emi.api.EmiRegistry;

public interface CompendiumModule {
    String id();

    void register(EmiRegistry registry);
}
