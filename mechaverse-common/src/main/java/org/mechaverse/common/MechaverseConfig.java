package org.mechaverse.common;

import java.net.URI;

import org.jboss.resteasy.client.ClientRequestFactory;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.api.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Common configuration.
 */
@Configuration
@PropertySources(value = {@PropertySource("classpath:/mechaverse.properties")})
public class MechaverseConfig {

  @Value("${org.mechaverse.managerUrl}")
  private String managerUrl;

  @Value("${org.mechaverse.storageServiceUrl}")
  private String storageServiceUrl;

  @Autowired(required = false) SimulationService simulationService;

  public SimulationService getSimulationService() {
    return simulationService;
  }

  @Bean
  public MechaverseManager getManager() {
    ClientRequestFactory clientFactory = new ClientRequestFactory(URI.create(managerUrl));
    return clientFactory.createProxy(MechaverseManager.class);
  }

  @Bean
  public MechaverseStorageService getStorageService() {
    ClientRequestFactory clientFactory = new ClientRequestFactory(URI.create(storageServiceUrl));
    return clientFactory.createProxy(MechaverseStorageService.class);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
     return new PropertySourcesPlaceholderConfigurer();
  }
}
