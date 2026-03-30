package it.aruba.delivery.request.exception;

public class InvalidRecipientException extends RuntimeException {
	public InvalidRecipientException(String message) {
		super(message);
	}
}