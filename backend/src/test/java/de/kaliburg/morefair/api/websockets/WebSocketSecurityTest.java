package de.kaliburg.morefair.api.websockets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.api.AuthController;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.mail.services.EmailService;
import de.kaliburg.morefair.security.CookieSameSiteFilter;
import de.kaliburg.morefair.security.CustomCookieCsrfTokenRepository;
import de.kaliburg.morefair.security.SecurityUtils;
import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.messaging.web.csrf.XorCsrfChannelInterceptor;
import org.springframework.security.messaging.web.socket.server.CsrfTokenHandshakeInterceptor;
import org.springframework.security.web.method.annotation.CsrfTokenArgumentResolver;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.session.MapSessionRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.OriginHandshakeInterceptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class WebSocketSecurityTest {

  private final ChannelInterceptor subscriptionInterceptor = mock(ChannelInterceptor.class);
  private final WebSocketHandler webSocketHandler = mock(WebSocketHandler.class);
  private final ObjectMapper objectMapper = new ObjectMapper();
  private List<String> allowedOrigins;
  private CsrfTokenHandshakeInterceptor handshakeInterceptor;
  private XorCsrfChannelInterceptor csrfInterceptor;
  private TestChannelRegistration channelRegistration;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    WebSocketConfig config = new WebSocketConfig(subscriptionInterceptor);
    ReflectionTestUtils.setField(config, "sessionRepository",
        new MapSessionRepository(new ConcurrentHashMap<>()));
    StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
    StompWebSocketEndpointRegistration endpoint =
        mock(StompWebSocketEndpointRegistration.class, RETURNS_SELF);
    when(registry.addEndpoint("/socket/fair")).thenReturn(endpoint);
    config.configureStompEndpoints(registry);

    ArgumentCaptor<String[]> origins = ArgumentCaptor.forClass(String[].class);
    verify(endpoint).setAllowedOrigins(origins.capture());
    allowedOrigins = List.of(origins.getValue());
    ArgumentCaptor<HandshakeInterceptor[]> handshakes =
        ArgumentCaptor.forClass(HandshakeInterceptor[].class);
    verify(endpoint).addInterceptors(handshakes.capture());
    assertThat(handshakes.getValue()).hasSize(1);
    handshakeInterceptor = CsrfTokenHandshakeInterceptor.class.cast(handshakes.getValue()[0]);

    channelRegistration = new TestChannelRegistration();
    config.configureClientInboundChannel(channelRegistration);
    csrfInterceptor = channelRegistration.registeredInterceptors().stream()
        .filter(XorCsrfChannelInterceptor.class::isInstance)
        .map(XorCsrfChannelInterceptor.class::cast)
        .findFirst().orElseThrow();

    AuthController controller = new AuthController(mock(AccountService.class),
        mock(PasswordEncoder.class), mock(SecurityUtils.class), mock(EmailService.class),
        mock(FairConfig.class), mock(RequestThrottler.class));
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setCustomArgumentResolvers(new CsrfTokenArgumentResolver())
        .addFilters(new CookieSameSiteFilter(), csrfFilter())
        .build();
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://fair.kaliburg.de", "https://fairtest.kaliburg.de",
      "http://localhost:3000", "http://localhost:8080"})
  void acceptsApprovedOrigins(String origin) throws Exception {
    assertThat(checkOrigin(origin)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://attacker.example", "https://fair.kaliburg.de.attacker.example",
      "http://fair.kaliburg.de", "http://localhost:3001", "https://galaxy.click", "null"})
  void rejectsUnapprovedOrigins(String origin) throws Exception {
    assertThat(checkOrigin(origin)).isFalse();
  }

  @Test
  void validatesCsrfBeforeSessionAndSubscriptionInterceptors() {
    assertThat(channelRegistration.registeredInterceptors()).hasSize(3);
    assertThat(channelRegistration.registeredInterceptors().get(0)).isSameAs(csrfInterceptor);
    assertThat(channelRegistration.registeredInterceptors().get(2))
        .isSameAs(subscriptionInterceptor);
  }

  @Test
  void acceptsTheMaskedEndpointTokenOnConnect() throws Exception {
    CsrfResponse csrf = fetchCsrf();
    assertThat(csrf.token()).isNotEqualTo(csrf.cookie().getValue());
    Message<byte[]> connect = connectMessage(handshake(csrf.cookie()),
        csrf.headerName(), csrf.token());

    assertThat(csrfInterceptor.preSend(connect, new ExecutorSubscribableChannel()))
        .isSameAs(connect);
  }

  @Test
  void preservesTheExistingRawCookieTokenForHttpRequests() throws Exception {
    CsrfResponse csrf = fetchCsrf();

    mockMvc.perform(post("/api/auth/login").secure(true)
            .cookie(csrf.cookie()).header(csrf.headerName(), csrf.cookie().getValue()))
        .andExpect(status().isOk());
  }

  @Test
  void acceptsRefreshedTokensForTheSameCookie() throws Exception {
    CsrfResponse original = fetchCsrf();
    CsrfResponse refreshed = fetchCsrf(original.cookie());
    assertThat(refreshed.token()).isNotEqualTo(original.token());
    Message<byte[]> connect = connectMessage(handshake(original.cookie()),
        refreshed.headerName(), refreshed.token());

    assertThat(csrfInterceptor.preSend(connect, new ExecutorSubscribableChannel()))
        .isSameAs(connect);
  }

  @Test
  void issuesStrictCookiesForNormalPlay() throws Exception {
    MockHttpServletResponse response = mockMvc.perform(get("/api/auth/csrf").secure(true))
        .andExpect(status().isOk()).andReturn().getResponse();

    assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
        .containsIgnoringCase("SameSite=Strict").contains("Secure");
  }

  @Test
  void preservesIframeAccessWhenIssuingANewToken() throws Exception {
    MockHttpServletResponse response = mockMvc.perform(
            get("/api/auth/csrf").secure(true).header("is-iframed", "true"))
        .andExpect(status().isOk()).andReturn().getResponse();
    assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
        .contains("SameSite=None", "Secure");
    Cookie cookie = response.getCookie("XSRF-TOKEN");
    assertThat(cookie).isNotNull();
    JsonNode body = objectMapper.readTree(response.getContentAsString());
    Message<byte[]> connect = connectMessage(handshake(cookie),
        body.get("headerName").asText(), body.get("token").asText());

    assertThat(csrfInterceptor.preSend(connect, new ExecutorSubscribableChannel()))
        .isSameAs(connect);
  }

  @Test
  void rejectsMissingOrInvalidConnectTokens() throws Exception {
    CsrfResponse csrf = fetchCsrf();
    Map<String, Object> attributes = handshake(csrf.cookie());
    for (String token : new String[] {null, "invalid", csrf.cookie().getValue()}) {
      Message<byte[]> connect = connectMessage(attributes, csrf.headerName(), token);
      assertThatThrownBy(() ->
          csrfInterceptor.preSend(connect, new ExecutorSubscribableChannel()))
          .isInstanceOf(InvalidCsrfTokenException.class);
    }
  }

  @Test
  void rejectsTokensFromAnotherCookie() throws Exception {
    CsrfResponse first = fetchCsrf();
    CsrfResponse second = fetchCsrf();
    Message<byte[]> connect = connectMessage(handshake(second.cookie()),
        first.headerName(), first.token());

    assertThatThrownBy(() ->
        csrfInterceptor.preSend(connect, new ExecutorSubscribableChannel()))
        .isInstanceOf(InvalidCsrfTokenException.class);
  }

  @Test
  void rejectsConnectionsWithoutHandshakeCsrfState() {
    Message<byte[]> connect = connectMessage(new HashMap<>(), "X-XSRF-TOKEN", "anything");

    assertThatThrownBy(() ->
        csrfInterceptor.preSend(connect, new ExecutorSubscribableChannel()))
        .isInstanceOf(MissingCsrfTokenException.class);
  }

  private boolean checkOrigin(String origin) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/socket/fair");
    request.setScheme("https");
    request.setServerName("fair.kaliburg.de");
    request.setServerPort(443);
    request.addHeader(HttpHeaders.ORIGIN, origin);
    return new OriginHandshakeInterceptor(allowedOrigins).beforeHandshake(
        new ServletServerHttpRequest(request),
        new ServletServerHttpResponse(new MockHttpServletResponse()),
        webSocketHandler, new HashMap<>());
  }

  private CsrfResponse fetchCsrf(Cookie... cookies) throws Exception {
    var request = get("/api/auth/csrf").secure(true);
    if (cookies.length > 0) {
      request.cookie(cookies);
    }
    var result = mockMvc.perform(request)
        .andExpect(status().isOk()).andReturn();
    MockHttpServletResponse response = result.getResponse();
    assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).contains("no-store");
    JsonNode body = objectMapper.readTree(response.getContentAsString());
    Cookie cookie = cookies.length == 0 ? response.getCookie("XSRF-TOKEN") : cookies[0];
    assertThat(cookie).isNotNull();
    return new CsrfResponse(cookie, body.get("headerName").asText(), body.get("token").asText());
  }

  private Map<String, Object> handshake(Cookie cookie) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/socket/fair");
    request.setCookies(cookie);
    MockHttpServletResponse response = new MockHttpServletResponse();
    Map<String, Object> attributes = new HashMap<>();
    csrfFilter().doFilter(request, response, (req, res) ->
        assertThat(handshakeInterceptor.beforeHandshake(new ServletServerHttpRequest(request),
            new ServletServerHttpResponse(response), webSocketHandler, attributes)).isTrue());
    return attributes;
  }

  private CsrfFilter csrfFilter() {
    CsrfFilter filter = new CsrfFilter(new CustomCookieCsrfTokenRepository());
    filter.setRequestHandler(new XorCsrfTokenRequestAttributeHandler()::handle);
    return filter;
  }

  private Message<byte[]> connectMessage(Map<String, Object> attributes,
      String headerName, String token) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.setSessionAttributes(attributes);
    if (token != null) {
      accessor.setNativeHeader(headerName, token);
    }
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  private record CsrfResponse(Cookie cookie, String headerName, String token) {
  }

  private static class TestChannelRegistration extends ChannelRegistration {

    List<ChannelInterceptor> registeredInterceptors() {
      return getInterceptors();
    }
  }
}
