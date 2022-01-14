package de.kaliburg.morefair.controller;

import de.kaliburg.morefair.dto.EventDTO;
import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.messages.WSMessage;
import de.kaliburg.morefair.persistence.entity.Account;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.RankerService;
import de.kaliburg.morefair.utils.WSUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@Log4j2
public class RankerController {
    public static final String LADDER_DESTINATION = "/queue/ladder/";
    public static final String LADDER_UPDATE_DESTINATION = "/topic/ladder/";
    private final RankerService rankerService;
    private final AccountService accountService;
    private final WSUtils wsUtils;

    public RankerController(RankerService rankerService, AccountService accountService, WSUtils wsUtils) {
        this.rankerService = rankerService;
        this.accountService = accountService;
        this.wsUtils = wsUtils;
    }


    @MessageMapping("/ladder/init/{number}")
    public void initChat(SimpMessageHeaderAccessor sha, WSMessage wsMessage, @DestinationVariable("number") Integer number) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/ladder/init/{} from {}", number, uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, HttpStatus.FORBIDDEN);
            if (number <= rankerService.findHighestRankerByAccount(account).getLadder().getNumber()) {
                LadderViewDTO l = rankerService.findAllRankerByHighestLadderAreaAndAccount(account);
                wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, l);
            } else {
                wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IllegalArgumentException e) {
            wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/bias")
    public void buyBias(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/ladder/post/bias from {}", uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account != null)
                rankerService.addEvent(rankerService.findHighestRankerByAccount(account).getLadder().getNumber(),
                        new EventDTO(EventDTO.EventType.BIAS, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/multi")
    public void buyMulti(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/ladder/post/multi from {}", uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account != null)
                rankerService.addEvent(rankerService.findHighestRankerByAccount(account).getLadder().getNumber(),
                        new EventDTO(EventDTO.EventType.MULTI, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/vinegar")
    public void throwVinegar(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/ladder/post/vinegar from {}", uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account != null)
                rankerService.addEvent(rankerService.findHighestRankerByAccount(account).getLadder().getNumber(),
                        new EventDTO(EventDTO.EventType.VINEGAR, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/promote")
    public void promote(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/ladder/post/promote from {}", uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account != null)
                rankerService.addEvent(rankerService.findHighestRankerByAccount(account).getLadder().getNumber(),
                        new EventDTO(EventDTO.EventType.PROMOTE, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/ladder/post/asshole")
    public void beAsshole(SimpMessageHeaderAccessor sha, WSMessage wsMessage) {
        try {
            String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
            log.debug("/app/ladder/post/asshole from {}", uuid);
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account != null)
                rankerService.addEvent(rankerService.findHighestRankerByAccount(account).getLadder().getNumber(),
                        new EventDTO(EventDTO.EventType.ASSHOLE, account.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }


    // OLD HTTP REQUESTS

    /*
    @GetMapping(value = "/fair/ranker", produces = "application/json")
    public ResponseEntity<LadderViewDTO> getLadder(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("GET /fair/rankers from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>(rankerService.findAllRankerByHighestLadderAreaAndAccount(account), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Couldn't parse the UUID from {} using 'POST /fair/login {}'", request.getRemoteAddr(), uuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/fair/ranker/bias", produces = "application/json")
    public ResponseEntity<Void> buyBias(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("POST /fair/ranker/buy from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                if (rankerService.buyBias(account)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/fair/ranker/multiplier", produces = "application/json")
    public ResponseEntity<Void> buyMulti(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("POST /fair/ranker/multiplier from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                if (rankerService.buyMulti(account)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/fair/ranker/promote", produces = "application/json")
    public ResponseEntity<Void> promote(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("POST /fair/ranker/promote from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                if (rankerService.promote(account)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/fair/ranker/asshole", produces = "application/json")
    public ResponseEntity<Void> beAsshole(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("POST /fair/ranker/asshole from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                if (rankerService.beAsshole(account)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "fair/ranker/vinegar", produces = "application/json")
    public ResponseEntity<Void> throwVinegar(@CookieValue(name = "_uuid", defaultValue = "") String uuid, HttpServletRequest request) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        log.debug("POST /fair/ranker/vinegar from {}", uuid);
        try {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            DatabaseWriteSemaphore.getInstance().acquire();
            try {
                if (rankerService.throwVinegar(account)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } finally {
                DatabaseWriteSemaphore.getInstance().release();
            }
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    */
}
