package io.github.raffaeleflorio.ticketservice;

import io.smallrye.mutiny.Uni;

import javax.json.JsonObject;
import java.util.UUID;

/**
 * An event
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
public interface Event {

  /**
   * Emits its {@link JsonObject} representation
   *
   * @return Its {@link JsonObject} representation
   */
  Uni<JsonObject> asJsonObject();

  /**
   * Books a ticket
   *
   * @param participant The participant id
   * @return The ticket's id
   */
  Uni<UUID> ticket(UUID participant);
}
