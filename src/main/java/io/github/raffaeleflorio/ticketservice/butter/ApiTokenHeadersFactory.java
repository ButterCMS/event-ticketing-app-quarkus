package io.github.raffaeleflorio.ticketservice.butter;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A {@link ClientHeadersFactory} that write token in the Authorization header
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
@ApplicationScoped
final class ApiTokenHeadersFactory implements ClientHeadersFactory {

  private final String token;

  /**
   * Builds the headers factory
   *
   * @param token The token
   */
  public ApiTokenHeadersFactory(@ConfigProperty(name = "buttercms-api.token") final String token) {
    this.token = token;
  }

  @Override
  public MultivaluedMap<String, String> update(
    final MultivaluedMap<String, String> incomingHeaders,
    final MultivaluedMap<String, String> clientHeaders
  ) {
    var result = new MultivaluedHashMap<String, String>();
    result.add("Authorization", "Token ".concat(this.token));
    return result;
  }
}
