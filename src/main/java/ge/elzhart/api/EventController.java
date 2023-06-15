package ge.elzhart.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;

import ge.elzhart.model.domain.user.Role;
import ge.elzhart.service.EmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/sse/transactions")
@RolesAllowed(Role.OWNER)
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Use for subscriptions to sse transaction events")
public class EventController {

    private final EmitterService emitterService;

    @GetMapping
    @Operation(summary = "subscribe to sse transaction events")
    public SseEmitter subscribeToEvents(Principal principal) {
        log.debug("Subscribing member with id {}", principal.getName());
        return emitterService.createEmitter(principal.getName());
    }
}
