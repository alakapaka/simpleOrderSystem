package superyuda.ordering_system.exceptions;

public class CustomerNotExistException extends Throwable{
    public CustomerNotExistException(String message) {
        super(message);
    }
}
