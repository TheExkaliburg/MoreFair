package de.kaliburg.morefair.api.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Helper class to get the IP address of a request.
 */
@Component
public class HttpUtils {

  /**
   * Returns the IP address of the client that sent the request.
   *
   * @param request The request to get the IP address from.
   * @return The IP address of the client that sent the request.
   * @throws UnknownHostException If the IP address of the request cannot be determined.
   */
  public static Integer getIp(HttpServletRequest request) throws UnknownHostException {
    String ipString = request.getHeader("X-Forwarded-For");
    if (ipString == null || ipString.length() == 0 || "unknown".equalsIgnoreCase(ipString)) {
      ipString = request.getRemoteAddr();
    }

    return new BigInteger(InetAddress.getByName(ipString).getAddress()).intValue();
  }

  /**
   * Returns the IP address of the client that sent the request.
   *
   * @param request The request to get the IP address from.
   * @return The IP address of the client that sent the request.
   * @throws UnknownHostException If the IP address of the request cannot be determined.
   */
  public static Integer getIp(ServerHttpRequest request) throws UnknownHostException {
    String ipString = request.getHeaders()
        .getOrDefault("x-forwarded-for", List.of(request.getRemoteAddress().getHostName()))
        .get(0);

    return new BigInteger(InetAddress.getByName(ipString).getAddress()).intValue();
  }

  public static URI createCreatedUri(String apiPath) {
    return URI.create(
        ServletUriComponentsBuilder.fromCurrentContextPath().path(apiPath).toUriString());
  }

  public static ResponseEntity<Map<String, String>> buildErrorMessage(HttpStatus status,
      String message) {
    Map<String, String> errors = new HashMap<>();
    errors.put("message", message);
    return ResponseEntity.status(status).body(errors);
  }
}
