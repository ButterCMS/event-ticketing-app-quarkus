package io.github.raffaeleflorio.ticketservice.database.events;

import io.github.raffaeleflorio.ticketservice.Event;
import io.github.raffaeleflorio.ticketservice.Events;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link Events} backed by a relational database
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
@ApplicationScoped
final class DbEvents implements Events {

  private final ConnectionFactory connectionFactory;
  private final String eventsIdSelectQuery;
  private final Function<UUID, Event> eventFn;
  private final Supplier<UUID> newEventIdSupplier;
  private final Supplier<OffsetDateTime> nowSupplier;

  /**
   * Builds events
   *
   * @param connectionFactory The connection factory
   * @author Raffaele Florio (raffaeleflorio@protonmail.com)
   */
  @Inject
  DbEvents(final ConnectionFactory connectionFactory) {
    this(
      connectionFactory,
      "SELECT ID FROM EVENTS",
      id -> new DbEvent(id, connectionFactory, () -> OffsetDateTime.now(ZoneOffset.UTC)),
      UUID::randomUUID,
      () -> OffsetDateTime.now(ZoneOffset.UTC)
    );
  }

  DbEvents(
    final ConnectionFactory connectionFactory,
    final String eventsIdSelectQuery,
    final Function<UUID, Event> eventFn,
    final Supplier<UUID> newEventIdSupplier,
    final Supplier<OffsetDateTime> nowSupplier
  ) {
    this.connectionFactory = connectionFactory;
    this.eventsIdSelectQuery = eventsIdSelectQuery;
    this.eventFn = eventFn;
    this.newEventIdSupplier = newEventIdSupplier;
    this.nowSupplier = nowSupplier;
  }

  @Override
  public Uni<Event> event(final JsonObject event) {
    var id = this.newEventIdSupplier.get();
    return this.statement(
        "INSERT INTO EVENTS",
        "(ID, EXTERNAL_ID, ORIGIN, TITLE, DESCRIPTION, POSTER, EVENT_TIMESTAMP, MAX_TICKETS, CREATION_TIMESTAMP)",
        "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)"
      )
      .onItem().transformToMulti(statement -> statement
        .bind("$1", id)
        .bind("$2", event.getString("externalId"))
        .bind("$3", event.getString("origin"))
        .bind("$4", event.getString("title"))
        .bind("$5", event.getString("description"))
        .bind("$6", event.getString("poster"))
        .bind("$7", OffsetDateTime.parse(event.getString("date")))
        .bind("$8", event.getInt("maxTickets"))
        .bind("$9", this.nowSupplier.get())
        .execute()
      )
      .onItem().transformToMultiAndMerge(Result::getRowsUpdated)
      .filter(rowsUpdated -> rowsUpdated > 0)
      .onItem().transform(rowsUpdated -> id)
      .onItem().transform(this.eventFn)
      .onCompletion().ifEmpty().failWith(new RuntimeException("Unable to add an event"))
      .toUni();
  }

  private Uni<Statement> statement(final String... pieces) {
    return this.connection()
      .onItem().transform(connection -> connection.createStatement(String.join(" ", pieces)));
  }

  private Uni<Connection> connection() {
    return Uni.createFrom().publisher(this.connectionFactory.create());
  }

  @Override
  public Multi<Event> event(final UUID id) {
    return this.statement(
        this.eventsIdSelectQuery,
        this.eventsIdSelectQuery.contains(" WHERE ") ? "AND ID = $1" : "WHERE ID = $1"
      )
      .onItem().transformToMulti(statement -> statement.bind("$1", id).execute())
      .onItem().transformToMultiAndMerge(result -> result.map((row, rowMetadata) -> row.get("ID", UUID.class)))
      .onItem().transform(this.eventFn);
  }

  @Override
  public Events upcoming() {
    return new DbEvents(
      this.connectionFactory,
      "SELECT ID FROM EVENTS WHERE EVENT_TIMESTAMP >= NOW()",
      this.eventFn,
      this.newEventIdSupplier,
      this.nowSupplier
    );
  }

  @Override
  public Uni<JsonObject> asJsonObject() {
    return this.statement(this.eventsIdSelectQuery)
      .onItem().transformToMulti(Statement::execute)
      .onItem().transformToMultiAndMerge(result -> result.map((row, rowMetadata) -> row.get("ID", UUID.class)))
      .onItem().transform(this.eventFn)
      .onItem().transformToUniAndMerge(Event::asJsonObject)
      .collect().in(Json::createArrayBuilder, JsonArrayBuilder::add)
      .onItem().transform(events -> Json.createObjectBuilder().add("events", events).build());
  }
}
