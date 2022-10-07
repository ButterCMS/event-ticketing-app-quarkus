package io.github.raffaeleflorio.ticketservice.butter;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

/**
 * An identity provider to authenticate Butter according a shared secret
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
@ApplicationScoped
final class XButterKeyIdentityProvider implements IdentityProvider<XButterKeyAuthenticationRequest> {

  private final String secret;

  /**
   * Builds the provider
   *
   * @param secret The shared secret value
   */
  XButterKeyIdentityProvider(@ConfigProperty(name = "buttercms-webhook.secret") final String secret) {
    this.secret = secret;
  }

  @Override
  public Class<XButterKeyAuthenticationRequest> getRequestType() {
    return XButterKeyAuthenticationRequest.class;
  }

  @Override
  public Uni<SecurityIdentity> authenticate(
    final XButterKeyAuthenticationRequest xButterKeyAuthenticationRequest,
    final AuthenticationRequestContext authenticationRequestContext
  ) {
    if (xButterKeyAuthenticationRequest.verify(request -> request.equals(this.secret))) {
      return this.butterSecurityIdentity();
    }
    return Uni.createFrom().nullItem();
  }

  private Uni<SecurityIdentity> butterSecurityIdentity() {
    return Uni.createFrom().item(
      QuarkusSecurityIdentity.builder()
        .setPrincipal(() -> "butter")
        .addRole("BUTTER")
        .build()
    );
  }
}
