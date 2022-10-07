package io.github.raffaeleflorio.ticketservice;

import io.smallrye.mutiny.Uni;

/**
 * A new event
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
public interface NewEvent {

  /**
   * Updates a collection of event
   *
   * @param events The events to update
   * @return Nothing
   */
  Uni<Void> update(Events events);
}
