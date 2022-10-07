package io.github.raffaeleflorio.ticketservice.butter;

import io.quarkus.security.identity.request.BaseAuthenticationRequest;

import java.util.function.Predicate;

/**
 * A Butter authentication request backed by the X-BUTTER-KEY header
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
final class XButterKeyAuthenticationRequest extends BaseAuthenticationRequest {

  private final String value;

  /**
   * Builds the request
   *
   * @param value The request value
   */
  XButterKeyAuthenticationRequest(final String value) {
    this.value = value;
  }

  /**
   * Verify itself according a predicate
   *
   * @param predicate The predicate to satisfy
   * @return True if the request satisfy the predicate
   */
  Boolean verify(final Predicate<String> predicate) {
    return predicate.test(this.value);
  }
}
