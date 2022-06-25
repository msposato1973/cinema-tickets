package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketConstant;
import uk.gov.dwp.uc.pairtest.domain.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.ErrorMessage;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

	private TicketPaymentService ticketPaymentService;

	private SeatReservationService seatReservationService;

	private int totalAmountToPay;
	private int totalSeatsToAllocate;
	private boolean thereIsAnAdult;
	private int totalTickets;

	/**
	 * Should only have private methods other than the one below.
	 */
	@Override
	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
			throws InvalidPurchaseException {
		totalAmountToPay = 0;
		totalSeatsToAllocate = 0;
		thereIsAnAdult = false;
		totalTickets = 0;

		if (ticketTypeRequests.length > 0) {
			if ((accountId != null) && (accountId > 0)) {
				Arrays.stream(ticketTypeRequests).forEach(x -> processTicketTypeRequest(x));
				if ((totalTickets > 0) && (!thereIsAnAdult)) {
					throw new InvalidPurchaseException(ErrorMessage.ADULT_MISSING.getMessage());
				}
				if (totalTickets > 0) {
					processTicketPayment(accountId);
					processSeatReservation(accountId);
				}
			} else {
				throw new InvalidPurchaseException(ErrorMessage.INVALID_ACCOUNT.getMessage());
			}
		}
	}

	private void processTicketTypeRequest(TicketTypeRequest currentRequest) throws InvalidPurchaseException {
		totalTickets += currentRequest.getNoOfTickets();
		if (totalTickets > TicketConstant.MAX_TICKETS) {
			throw new InvalidPurchaseException(ErrorMessage.ONLY_MAX_SEATS_AT_THE_TIME.getMessage());
		}
		if (currentRequest.getTicketType() != TicketTypeRequest.Type.INFANT) {
			totalSeatsToAllocate += currentRequest.getNoOfTickets();
			totalAmountToPay += currentRequest.getNoOfTickets()
					* TicketPrice.getTicketPrice(currentRequest.getTicketType());
			if (currentRequest.getTicketType() == TicketTypeRequest.Type.ADULT) {
				thereIsAnAdult = true;
			}
		}
	}

	private void processSeatReservation(Long accountId) throws InvalidPurchaseException {
		try {

			seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);

		} catch (RuntimeException e) {
			throw new InvalidPurchaseException(
					ErrorMessage.SEAT_RESERVATION_UNEXPECTED_ERROR.getMessage() + e.getMessage());
		}
	}

	private void processTicketPayment(Long accountId) throws InvalidPurchaseException {
		try {

			ticketPaymentService.makePayment(accountId, totalAmountToPay);

		} catch (RuntimeException e) {
			throw new InvalidPurchaseException(
					ErrorMessage.TICKET_PAYMENT_UNEXPECTED_ERROR.getMessage() + e.getMessage());
		}
	}
}