package io.github.raffaeleflorio.ticketservice;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.json.JsonObject;
import java.util.UUID;

/**
 * A collection of {@link Event}
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
public interface Events {

  /**
   * Adds an event to itself given a JSON description.
   * The description must have:
   * <ul>
   *  <li>title: the event's title as string</li>
   *  <li>description: the event's description as string</li>
   *  <li>poster: the event's poster as URL</li>
   *  <li>date: the event's date as ISO-8601 date-time</li>
   *  <li>maxTickets: the event's max tickets as integer</li>
   *  <li>origin: the event's origin as string. That is who are adding it (e.g. BUTTER)</li>
   *  <li>externalId: the event's external id. That is the id used by the origin</li>
   * </ul>
   *
   * @param event The specification to create the event
   * @return The added event
   */
  Uni<Event> event(JsonObject event);

  /**
   * Emits an event corresponded to the given id
   *
   * @param id The event's id
   * @return The event or empty
   */
  Multi<Event> event(UUID id);

  /**
   * Filters out gone events
   *
   * @return All upcoming events
   */
  Events upcoming();

  /**
   * Emits its {@link JsonObject} representation
   *
   * @return Its {@link JsonObject} representation
   */
  Uni<JsonObject> asJsonObject();
}
