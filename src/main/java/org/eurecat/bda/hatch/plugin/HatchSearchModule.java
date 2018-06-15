package org.eurecat.bda.hatch.plugin;

import org.elasticsearch.common.inject.AbstractModule;

public class HatchSearchModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ControllerSingleton.class).asEagerSingleton();
    }
}
