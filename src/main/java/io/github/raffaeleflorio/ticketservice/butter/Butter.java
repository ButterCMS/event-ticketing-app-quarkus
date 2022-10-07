package io.github.raffaeleflorio.ticketservice.butter;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.json.JsonObject;
import javax.ws.rs.*;

/**
 * Butter's API
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 * @see <a href="https://buttercms.com/docs/api/">API documentation</a>
 */
@RegisterRestClient(baseUri = "https://api.buttercms.com/v2")
@RegisterClientHeaders(ApiTokenHeadersFactory.class)
@Consumes("application/json")
@Produces("application/json")
public interface Butter {

  @GET
  @Path("/pages/{page_type_slug}/{page_slug}/")
  Uni<JsonObject> getSinglePage(
    @PathParam("page_type_slug") String pageTypeSlug,
    @PathParam("page_slug") String pageSlug
  );
}
