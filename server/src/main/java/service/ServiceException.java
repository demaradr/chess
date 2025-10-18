package service;

public class ServiceException extends Exception{

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable error) {
        super(message, error);
    }
}
