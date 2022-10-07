package io.github.raffaeleflorio.ticketservice.butter;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Set;

/**
 * A mechanism to authenticate Butter through the X-BUTTER-KEY header
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
@ApplicationScoped
final class XButterKeyAuthenticationMechanism implements HttpAuthenticationMechanism {

  @Override
  public Uni<SecurityIdentity> authenticate(
    final RoutingContext context,
    final IdentityProviderManager identityProviderManager
  ) {
    if (context.request().headers().contains("X-BUTTER-KEY")) {
      var request = new XButterKeyAuthenticationRequest(context.request().getHeader("X-BUTTER-KEY"));
      return identityProviderManager.authenticate(request);
    }
    return Uni.createFrom().nullItem();
  }

  @Override
  public Uni<ChallengeData> getChallenge(final RoutingContext context) {
    return Uni.createFrom().item(
      new ChallengeData(
        401,
        "WWW-Authenticate",
        "Butter"
      )
    );
  }

  @Override
  public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
    return Collections.singleton(XButterKeyAuthenticationRequest.class);
  }
}
