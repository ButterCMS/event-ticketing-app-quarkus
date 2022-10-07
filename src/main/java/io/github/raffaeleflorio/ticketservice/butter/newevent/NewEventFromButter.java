package io.github.raffaeleflorio.ticketservice.butter.newevent;

import io.github.raffaeleflorio.ticketservice.Events;
import io.github.raffaeleflorio.ticketservice.NewEvent;
import io.github.raffaeleflorio.ticketservice.butter.Butter;
import io.smallrye.mutiny.Uni;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * A {@link NewEvent} received through {@link Butter}
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
final class NewEventFromButter implements NewEvent {

  private final String id;
  private final Butter butter;

  /**
   * Builds a new event
   *
   * @param id     The event id
   * @param butter Butter's API
   */
  NewEventFromButter(final String id, final Butter butter) {
    this.id = id;
    this.butter = butter;
  }

  @Override
  public Uni<Void> update(final Events events) {
    return this.butter
      .getSinglePage("event", this.id)
      .onItem().transform(eventPage -> eventPage.getJsonObject("data"))
      .onItem().transform(eventData -> Json.createObjectBuilder()
        .add("origin", "BUTTER")
        .add("externalId", this.slug(eventData))
        .add("title", this.title(eventData))
        .add("description", this.description(eventData))
        .add("poster", this.poster(eventData))
        .add("date", this.date(eventData))
        .add("maxTickets", this.maxTickets(eventData))
      )
      .onItem().transform(JsonObjectBuilder::build)
      .onItem().transformToUni(events::event)
      .onItem().ignore().andContinueWithNull();
  }

  private String slug(final JsonObject eventData) {
    return eventData.getString("slug");
  }

  private String title(final JsonObject eventData) {
    return this.eventFields(eventData).getString("title");
  }

  private JsonObject eventFields(final JsonObject eventData) {
    return eventData.getJsonObject("fields");
  }

  private String description(final JsonObject eventData) {
    return this.eventFields(eventData).getString("description");
  }

  private String poster(final JsonObject eventData) {
    return this.eventFields(eventData).getString("poster");
  }

  private String date(final JsonObject eventData) {
    var date = this.eventFields(eventData).getString("date");
    return LocalDateTime.parse(date).atOffset(ZoneOffset.UTC).toString();
  }

  private Integer maxTickets(final JsonObject eventData) {
    return Integer.parseInt(this.eventFields(eventData).getString("maxtickets"));
  }
}
