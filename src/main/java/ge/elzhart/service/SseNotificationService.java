package ge.elzhart.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;

import ge.elzhart.api.dto.TransactionEventDto;
import ge.elzhart.api.mapper.EventMapper;
import ge.elzhart.repository.EmitterRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Primary
@AllArgsConstructor
@Slf4j
public class SseNotificationService implements NotificationService {

    private final EmitterRepository emitterRepository;
    private final EventMapper eventMapper;

    @Override
    public void sendNotification(String memberId, TransactionEventDto event) {
        if (event == null) {
            log.debug("No server event to send to device.");
            return;
        }
        doSendNotification(memberId, event);
    }

    private void doSendNotification(String memberId, TransactionEventDto event) {
        emitterRepository.get(memberId).ifPresentOrElse(sseEmitter -> {
            try {
                log.debug("Sending event: {} for member: {}", event, memberId);
                sseEmitter.send(eventMapper.toSseEventBuilder(event));
            } catch (IOException | IllegalStateException e) {
                log.debug("Error while sending event: {} for member: {} - exception: {}", event, memberId, e);
                emitterRepository.remove(memberId);
            }
        }, () -> log.debug("No emitter for member {}", memberId));
    }

}
