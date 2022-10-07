package io.github.raffaeleflorio.ticketservice.butter.newevent;

import io.github.raffaeleflorio.ticketservice.Events;
import io.github.raffaeleflorio.ticketservice.NewEvent;
import io.github.raffaeleflorio.ticketservice.butter.Butter;
import io.smallrye.mutiny.Uni;

import javax.json.JsonObject;
import java.util.function.BiFunction;

/**
 * A {@link NewEvent} received through a Butter's webhook notification
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 * @see <a href="https://buttercms.com/docs/api/#feeds">Butter's webhook documentation</a>
 */
final class NewEventFromButterWebHook implements NewEvent {

  private final JsonObject notification;
  private final Butter butter;
  private final BiFunction<String, Butter, NewEvent> newEventFromButterFn;

  /**
   * Builds a new event
   *
   * @param notification The webhook's notification
   * @param butter       The Butter's API
   */
  NewEventFromButterWebHook(final JsonObject notification, final Butter butter) {
    this(notification, butter, NewEventFromButter::new);
  }

  NewEventFromButterWebHook(
    final JsonObject notification,
    final Butter butter,
    final BiFunction<String, Butter, NewEvent> newEventFromButterFn
  ) {
    this.notification = notification;
    this.butter = butter;
    this.newEventFromButterFn = newEventFromButterFn;
  }

  @Override
  public Uni<Void> update(final Events events) {
    if (this.pageType().equals("event")) {
      return this.newEventFromButterFn.apply(this.id(), this.butter).update(events);
    }
    return Uni.createFrom().voidItem();
  }

  private String pageType() {
    return this.notification
      .getJsonObject("data")
      .getString("page_type");
  }

  private String id() {
    return this.notification
      .getJsonObject("data")
      .getString("id");
  }
}
