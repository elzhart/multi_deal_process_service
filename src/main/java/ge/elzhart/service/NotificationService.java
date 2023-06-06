package ge.elzhart.service;


import ge.elzhart.api.dto.TransactionEventDto;

public interface NotificationService {

    void sendNotification(String memberId, TransactionEventDto event);
}
