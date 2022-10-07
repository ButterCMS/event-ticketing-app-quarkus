package io.github.raffaeleflorio.ticketservice.database;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;

@ApplicationScoped
class DbConnectionFactories {

  private final String url;

  DbConnectionFactories(@ConfigProperty(name = "r2dbc.url") final String url) {
    this.url = url;
  }

  @Produces
  @ApplicationScoped
  ConnectionFactory connectionFactory() {
    return ConnectionFactories.get(this.url);
  }
}
