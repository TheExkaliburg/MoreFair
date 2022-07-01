function replace(destination, placeholder, value) {
  let result = destination;
  result = result.replace(placeholder, value);
  console.log(destination, placeholder, value, result);
  return result;
}

const API = {
  ACCOUNT: {
    APP_LOGIN_DESTINATION: "/app/account/login",
    APP_RENAME_DESTINATION: "/app/account/name",
    QUEUE_LOGIN_DESTINATION: "/user/queue/account/login",
  },
  GAME: {
    APP_INIT_DESTINATION: (number) =>
      replace("/app/game/init/{number}", "{number}", number),
    APP_BIAS_DESTINATION: "/app/game/bias",
    APP_MULTI_DESTINATION: "/app/game/multi",
    APP_VINEGAR_DESTINATION: "/app/game/vinegar",
    APP_PROMOTE_DESTINATION: "/app/game/promote",
    APP_AUTOPROMOTE_DESTINATION: "/app/game/autopromote",
    TOPIC_EVENTS_DESTINATION: (number) =>
      replace("/topic/game/events/{number}", "{number}", number),
    QUEUE_INIT_DESTINATION: "/user/queue/game/init",
    PRIVATE_EVENTS_DESTINATION: "/private/game/events",
  },
  CHAT: {
    APP_INIT_DESTINATION: (number) =>
      replace("/app/chat/init/{number}", "{number}", number),
    APP_CHAT_DESTINATION: (number) =>
      replace("/app/chat/{number}", "{number}", number),
    TOPIC_EVENTS_DESTINATION: (number) =>
      replace("/topic/chat/events/{number}", "{number}", number),
    QUEUE_INIT_DESTINATION: "/user/queue/chat/init",
    PRIVATE_PROMPT_DESTINATION: "/private/chat/prompt",
  },
  FAIR: {
    APP_INFO_DESTINATION: "/app/info",
    QUEUE_INFO_DESTINATION: "/user/queue/info",
  },
  MODERATION: {
    APP_CHAT_INIT_DESTINATION: "/app/mod/chat/init",
    APP_BAN_DESTINATION: (id) => replace("/app/mod/ban/{id}", "{id}", id),
    APP_MUTE_DESTINATION: (id) => replace("/app/mod/mute/{id}", "{id}", id),
    APP_FREE_DESTINATION: (id) => replace("/app/mod/free/{id}", "{id}", id),
    APP_RENAME_DESTINATION: (id) => replace("/app/mod/rename/{id}", "{id}", id),
    APP_CONFIRM_DESTINATION: (id) =>
      replace("/app/mod/prompt/{id}", "{id}", id),
    APP_MOD_DESTINATION: (id) => replace("/app/mod/mod/{id}", "{id}", id),
    TOPIC_CHAT_EVENTS_DESTINATION: "/topic/mod/chat/events",
    TOPIC_EVENTS_DESTINATION: "/topic/mod/events",
    QUEUE_CHAT_INIT_DESTINATION: "/user/queue/mod/chat/init",
  },
};

export default API;
