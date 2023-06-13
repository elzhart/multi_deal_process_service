package ge.elzhart.exception;

public class AlreadyExistException extends RuntimeException {

    public AlreadyExistException(String message) {
        super(message);
    }

    public AlreadyExistException(Class<?> clazz, String id) {
        super(String.format("Entity %s with id %s already exist", clazz.getSimpleName(), id));
    }
}
