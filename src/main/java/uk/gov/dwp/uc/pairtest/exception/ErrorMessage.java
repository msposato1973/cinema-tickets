package uk.gov.dwp.uc.pairtest.exception;

public enum ErrorMessage {
    INVALID_ACCOUNT("Invalid account id"),
    ADULT_MISSING("Child and Infant tickets cannot be purchased without purchasing an Adult ticket."),
    ONLY_MAX_SEATS_AT_THE_TIME("Only a maximum of 20 tickets that can be purchased at a time."),
    SEAT_RESERVATION_UNEXPECTED_ERROR("Seat reservation unexpected error: "),
    TICKET_PAYMENT_UNEXPECTED_ERROR("Ticket payment unexpected error");

    private String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return message;
    }
}
