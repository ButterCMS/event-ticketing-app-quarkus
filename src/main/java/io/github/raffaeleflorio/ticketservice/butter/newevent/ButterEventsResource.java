package io.github.raffaeleflorio.ticketservice.butter.newevent;

import io.github.raffaeleflorio.ticketservice.Events;
import io.github.raffaeleflorio.ticketservice.NewEvent;
import io.github.raffaeleflorio.ticketservice.butter.Butter;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.function.Function;

@Path("/butter/events")
public final class ButterEventsResource {

  private final Events events;
  private final Function<JsonObject, NewEvent> newEventFn;

  @Inject
  public ButterEventsResource(final Events events, @RestClient final Butter butter) {
    this(events, notification -> new NewEventFromButterWebHook(notification, butter));
  }

  ButterEventsResource(final Events events, final Function<JsonObject, NewEvent> newEventFn) {
    this.events = events;
    this.newEventFn = newEventFn;
  }

  @POST
  @RolesAllowed("BUTTER")
  public Uni<Void> addEvent(final JsonObject eventPage) {
    return this.newEventFn.apply(eventPage).update(this.events);
  }
}
