const API = {
  ACCOUNT: {
    APP_LOGIN_DESTINATION: "/account/login/",
    APP_RENAME_DESTINATION: "/account/name/",
    QUEUE_LOGIN_DESTINATION: "/account/name/",
  },
  GAME: {
    APP_INIT_DESTINATION: "/game/init/{number}",
    APP_BIAS_DESTINATION: "/game/bias",
    APP_MULTI_DESTINATION: "/game/multi",
    APP_VINEGAR_DESTINATION: "/game/vinegar",
    APP_PROMOTE_DESTINATION: "/game/promote",
    APP_AUTOPROMOTE_DESTINATION: "/game/autopromote",
    TOPIC_EVENTS_DESTINATION: "/game/events",
    QUEUE_INIT_DESTINATION: "/game/init",
    PRIVATE_EVENTS_DESTINATION: "/game/events",
  },
  CHAT: {
    APP_INIT_DESTINATION: "/chat/init/{number}",
    APP_CHAT_DESTINATION: "/chat/{number}",
    TOPIC_EVENTS_DESTINATION: "/chat/events/{number}",
    QUEUE_INIT_DESTINATION: "/chat/init",
    PRIVATE_PROMPT_DESTINATION: "/chat/prompt",
  },
  FAIR: {
    APP_INFO_DESTINATION: "/info",
    QUEUE_INFO_DESTINATION: "/info",
  },
  MODERATION: {
    APP_CHAT_INIT_DESTINATION: "/mod/chat/init",
    APP_BAN_DESTINATION: "/mod/ban/{id}",
    APP_MUTE_DESTINATION: "/mod/mute/{id}",
    APP_FREE_DESTINATION: "/mod/free/{id}",
    APP_RENAME_DESTINATION: "/mod/rename/{id}",
    APP_CONFIRM_DESTINATION: "/mod/prompt/{id}",
    APP_MOD_DESTINATION: "/mod/mod/{id}",
    TOPIC_CHAT_EVENTS_DESTINATION: "/mod/chat/events",
    TOPIC_EVENTS_DESTINATION: "/mod/events",
    QUEUE_CHAT_INIT_DESTINATION: "/mod/chat/init",
  },
};

export default API;
