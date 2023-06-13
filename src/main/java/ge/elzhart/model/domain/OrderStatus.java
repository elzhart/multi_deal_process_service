package ge.elzhart.model.domain;

public enum OrderStatus {
    DRAFT, // Order is created, but not published - because it need to add some info
    POSTED, // Order is ready for select by other users
    IN_TRANSACTION, // Order take part in transaction
    CLOSED // Order is closed, if it's used decide to remove it from order process
}
