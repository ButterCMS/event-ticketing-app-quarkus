package io.github.raffaeleflorio.ticketservice.database.events;

import io.github.raffaeleflorio.ticketservice.Event;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.reactivestreams.Publisher;

import javax.json.Json;
import javax.json.JsonObject;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * An {@link Event} backed by a relational database
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 */
final class DbEvent implements Event {

  private final UUID id;
  private final ConnectionFactory connectionFactory;
  private final Supplier<UUID> newTicketIdSupplier;
  private final Supplier<OffsetDateTime> nowSupplier;

  /**
   * Builds an event
   *
   * @param id                The event's id
   * @param connectionFactory The connection factory
   * @param nowSupplier       The supplier of now as an {@link OffsetDateTime}
   */
  DbEvent(final UUID id, final ConnectionFactory connectionFactory, final Supplier<OffsetDateTime> nowSupplier) {
    this(
      id,
      connectionFactory,
      UUID::randomUUID,
      nowSupplier
    );
  }

  DbEvent(
    final UUID id,
    final ConnectionFactory connectionFactory,
    final Supplier<UUID> newTicketIdSupplier,
    final Supplier<OffsetDateTime> nowSupplier
  ) {
    this.id = id;
    this.connectionFactory = connectionFactory;
    this.newTicketIdSupplier = newTicketIdSupplier;
    this.nowSupplier = nowSupplier;
  }

  @Override
  public Uni<JsonObject> asJsonObject() {
    return this.statement(
        "SELECT ID, TITLE, DESCRIPTION, POSTER, EVENT_TIMESTAMP, (MAX_TICKETS - SOLD_TICKETS) AS AVAILABLE_TICKETS",
        "FROM EVENTS",
        "WHERE ID = $1"
      )
      .onItem().transformToMulti(statement -> statement.bind("$1", this.id).execute())
      .onItem().transformToMultiAndMerge(this::asJsonObject)
      .onCompletion().ifEmpty().failWith(new RuntimeException("Unable to find the event"))
      .toUni();
  }

  private Uni<Statement> statement(final String... pieces) {
    return this.connection()
      .onItem().transform(connection -> this.statement(connection, pieces));
  }

  private Uni<Connection> connection() {
    return Uni.createFrom().publisher(this.connectionFactory.create());
  }

  private Statement statement(final Connection connection, final String... pieces) {
    return connection.createStatement(String.join(" ", pieces));
  }

  private Publisher<JsonObject> asJsonObject(final Result result) {
    return result.map((row, rowMetadata) -> Json.createObjectBuilder()
      .add("id", row.get("ID", UUID.class).toString())
      .add("title", row.get("TITLE", String.class))
      .add("description", row.get("DESCRIPTION", String.class))
      .add("poster", row.get("POSTER", String.class))
      .add("date", row.get("EVENT_TIMESTAMP", OffsetDateTime.class).toString())
      .add("availableTickets", row.get("AVAILABLE_TICKETS", Integer.class))
      .build());
  }

  @Override
  public Uni<UUID> ticket(final UUID participant) {
    var ticketId = this.newTicketIdSupplier.get();
    return this.connection()
      .onItem().call(connection -> Uni.createFrom().publisher(connection.setAutoCommit(false)))
      .onItem().call(connection -> Uni.createFrom().publisher(connection.beginTransaction()))
      .onItem().call(connection ->
        this.ticket(connection, ticketId, participant)
          .onItem().call(() -> Uni.createFrom().publisher(connection.commitTransaction()))
          .onFailure().call(() -> Uni.createFrom().publisher(connection.rollbackTransaction()))
      )
      .onItem().ignore().andSwitchTo(Uni.createFrom().item(ticketId));
  }

  private Uni<Void> ticket(final Connection connection, final UUID ticketId, final UUID participant) {
    var updateSoldTicketsStatement = this.statement(
        connection,
        "UPDATE EVENTS",
        "SET SOLD_TICKETS = SOLD_TICKETS + 1",
        "WHERE ID = $1 AND SOLD_TICKETS < MAX_TICKETS AND EVENT_TIMESTAMP >= NOW()"
      )
      .bind("$1", this.id);
    var insertTicketStatement = this.statement(
        connection,
        "INSERT INTO TICKETS",
        "(ID, EVENT_ID, PARTICIPANT_ID, CREATION_TIMESTAMP)",
        "VALUES ($1, $2, $3, $4)"
      )
      .bind("$1", ticketId)
      .bind("$2", this.id)
      .bind("$3", participant)
      .bind("$4", this.nowSupplier.get());
    return Multi.createBy().combining().streams(
        Multi.createFrom().publisher(updateSoldTicketsStatement.execute())
          .onItem().transformToMultiAndMerge(Result::getRowsUpdated),
        Multi.createFrom().publisher(insertTicketStatement.execute())
          .onItem().transformToMultiAndMerge(Result::getRowsUpdated)
      )
      .asTuple()
      .filter(rowsUpdated -> rowsUpdated.getItem1() > 0 && rowsUpdated.getItem2() > 0)
      .onCompletion().ifEmpty().failWith(new RuntimeException("Unable to book a ticket"))
      .onItem().ignore()
      .toUni();
  }
}
