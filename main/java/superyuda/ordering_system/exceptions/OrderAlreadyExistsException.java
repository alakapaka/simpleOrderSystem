package superyuda.ordering_system.exceptions;

public class OrderAlreadyExistsException extends Throwable {
    public OrderAlreadyExistsException(String message) {
        super(message);
    }
}
