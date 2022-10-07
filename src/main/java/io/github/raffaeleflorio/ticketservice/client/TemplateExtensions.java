package io.github.raffaeleflorio.ticketservice.client;

import io.quarkus.qute.TemplateExtension;

import javax.json.JsonObject;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Set of Qute template extension methods
 *
 * @author Raffaele Florio (raffaeleflorio@protonmail.com)
 * @see <a href="https://quarkus.io/guides/qute-reference#template_extension_methods">Qute reference guide</a>
 */
@TemplateExtension
public final class TemplateExtensions {

  /**
   * Retrieves an {@link OffsetDateTime} value to which a key maps
   *
   * @param jsonObject The JSON object
   * @param key        The key
   * @return The retrieved value
   */
  public static OffsetDateTime getOffsetDateTime(final JsonObject jsonObject, final String key) {
    return OffsetDateTime.parse(jsonObject.getString(key));
  }

  /**
   * Formats an {@link OffsetDateTime} according a pattern
   *
   * @param offsetDateTime The offset date time
   * @param pattern        The pattern
   * @return The formatted offset date time
   */
  public static String format(final OffsetDateTime offsetDateTime, final String pattern) {
    return offsetDateTime.format(DateTimeFormatter.ofPattern(pattern));
  }
}
