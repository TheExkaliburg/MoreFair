package de.kaliburg.morefair.security;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;

public class SessionUtils {

  public static UUID getUuid(HttpSession session) {
    if (session == null) {
      return null;
    }

    Object object = session.getAttribute("UUID");
    if (!(object instanceof UUID)) {
      return null;
    }

    return (UUID) object;
  }
}
