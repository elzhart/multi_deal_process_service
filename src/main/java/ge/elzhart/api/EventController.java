package ge.elzhart.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;

import ge.elzhart.service.EmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class EventController {

    private final EmitterService emitterService;

    @GetMapping
    public SseEmitter subscribeToEvents(Principal principal) {
        log.debug("Subscribing member with id {}", principal.getName());
        return emitterService.createEmitter(principal.getName());
    }

}
