package io.github.raffaeleflorio.ticketservice.client.events;

import io.github.raffaeleflorio.ticketservice.Event;
import io.github.raffaeleflorio.ticketservice.Events;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/events")
public final class EventsResource {

  private final Events events;
  private final Template eventsHTMLTemplate;
  private final Template eventHTMLTemplate;

  public EventsResource(
    final Events events,
    @Location("client/events.html") final Template eventsHTMLTemplate,
    @Location("client/event.html") final Template eventHTMLTemplate
  ) {
    this.events = events;
    this.eventsHTMLTemplate = eventsHTMLTemplate;
    this.eventHTMLTemplate = eventHTMLTemplate;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<JsonObject> upcomingEventsAsJson() {
    return this.events.upcoming().asJsonObject();
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Uni<TemplateInstance> upcomingEventsAsHtml() {
    return this.events.upcoming().asJsonObject()
      .onItem().transform(this.eventsHTMLTemplate::data);
  }


  @GET
  @Path("{id}")
  @Produces(MediaType.TEXT_HTML)
  public Uni<RestResponse<TemplateInstance>> eventAsHtmlFragment(@PathParam("id") final UUID id) {
    return this.events.event(id)
      .onItem().transformToUniAndMerge(Event::asJsonObject)
      .onItem().transform(this.eventHTMLTemplate::data)
      .onItem().transform(RestResponse::ok)
      .onCompletion().ifEmpty().continueWith(RestResponse.notFound())
      .toUni();
  }

  @POST
  @Path("/{id}/tickets")
  public Uni<RestResponse<Void>> bookTicket(
    @PathParam("id") final UUID id,
    @HeaderParam("participant") final UUID participant
  ) {
    return this.events.upcoming().event(id)
      .onItem().transformToUniAndMerge(event -> event.ticket(participant))
      .onItem().transform(ticketId -> RestResponse.<Void>accepted())
      .onFailure().recoverWithItem(() -> RestResponse.status(409))
      .onCompletion().ifEmpty().continueWith(RestResponse.notFound())
      .toUni();
  }
}
