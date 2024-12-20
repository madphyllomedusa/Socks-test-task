package test.backspark.socks.exeptionhandler;

public class NotEnoughSocksException extends RuntimeException {
    public NotEnoughSocksException(String message) {
        super(message);
    }
}
