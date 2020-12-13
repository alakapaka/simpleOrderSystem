package superyuda.ordering_system.exceptions;

public class OrderDoesntExistsException extends Exception {

    public OrderDoesntExistsException(String message) {
        super(message);
    }
}
