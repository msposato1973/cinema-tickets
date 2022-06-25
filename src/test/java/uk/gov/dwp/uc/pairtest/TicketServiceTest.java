package uk.gov.dwp.uc.pairtest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.ErrorMessage;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceTest {

	private static final Logger logger = Logger.getLogger(TicketServiceTest.class.getName());

	@InjectMocks
	private TicketService ticketService = new TicketServiceImpl();

	@Mock
	private SeatReservationService seatReservationService;

	@Mock
	private TicketPaymentService ticketPaymentService;

	@Test
	public void testTicketService_purchaseTicketsCase1() {
		logger.info("testTicketServiceCase1: Begin");
		try {
			assertNotNull(ticketService);
			ticketService.purchaseTickets(getAccountId(), getTicketTypeRequestsCase1());
			assert (isValidTicketPurchase(ticketService, getTicketPurchaseExpectedValuesCase1()));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testTicketService_purchaseTicketsCase2() {
		logger.info("testTicketServiceCase2: Begin");
		try {
			assertNotNull(ticketService);
			ticketService.purchaseTickets(getAccountId(), getTicketTypeRequestsCase2());
			assert (isValidTicketPurchase(ticketService, getTicketPurchaseExpectedValuesCase2()));
		} catch (RuntimeException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testTicketServiceCase3() {
		logger.info("testTicketServiceCase3: Begin");
		try {
			assertNotNull(ticketService);
			ticketService.purchaseTickets(getAccountId());
			assert (isValidTicketPurchase(ticketService, getTicketPurchaseExpectedValuesCase3()));
		} catch (RuntimeException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testTicketService_purchaseTicketsCase4() {
		logger.info("testTicketServiceCase4: Begin");
		try {
			assertNotNull(ticketService);
			ticketService.purchaseTickets(getAccountId(), getTicketTypeRequestsCase4());

			assert (isValidTicketPurchase(ticketService, getTicketPurchaseExpectedValuesCase4()));
		} catch (RuntimeException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testTicketServiceExceptionInvalidAccount() {
		logger.info("testTicketServiceExceptionInvalidAccount: Begin");
		try {
			assertNotNull(ticketService);
			ticketService.purchaseTickets(Long.valueOf(0), getTicketTypeRequestsCase1());
		} catch (InvalidPurchaseException e) {
			assert (e.getMessage().equals(ErrorMessage.INVALID_ACCOUNT.getMessage()));
		} catch (RuntimeException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testTicketServiceExceptionAdultMissing() {
		logger.info("testTicketServiceExceptionAdultMissing: Begin");
		try {
			assertNotNull(ticketService);
			ticketService.purchaseTickets(getAccountId(), getTicketTypeRequestsExceptionAdultMissing());
		} catch (InvalidPurchaseException e) {
			assert (e.getMessage().equals(ErrorMessage.ADULT_MISSING.getMessage()));
		} catch (RuntimeException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testTicketServiceExceptionMaxTickets() {
		logger.info("testTicketServiceExceptionMaxTickets: Begin");
		try {
			assertNotNull(ticketService);
			ticketService.purchaseTickets(getAccountId(), this.getTicketTypeRequestsExceptionMaxTickets());
		} catch (InvalidPurchaseException e) {
			assert (e.getMessage().equals(ErrorMessage.ONLY_MAX_SEATS_AT_THE_TIME.getMessage()));
		} catch (RuntimeException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testTicketServiceExceptionTicketPaymentServiceUnexpectedError() {
		logger.info("testTicketServiceExceptionTicketPaymentServiceUnexpectedError: Begin");
		try {
			assertNotNull(ticketService);
			doThrow(new RuntimeException()).when(ticketPaymentService).makePayment(anyLong(), anyInt());
			ticketService.purchaseTickets(getAccountId(), getTicketTypeRequestsCase1());
		} catch (InvalidPurchaseException e) {
			logger.info((e.getMessage()));
			assert (e.getMessage().startsWith(ErrorMessage.TICKET_PAYMENT_UNEXPECTED_ERROR.getMessage()));
		} catch (RuntimeException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testTicketServiceExceptionSeatReservationServiceUnexpectedError() {
		logger.info("testTicketServiceExceptionSeatReservationServiceUnexpectedError: Begin");
		try {
			assertNotNull(ticketService);
			doThrow(new RuntimeException()).when(seatReservationService).reserveSeat(anyLong(), anyInt());
			ticketService.purchaseTickets(getAccountId(), getTicketTypeRequestsCase1());
		} catch (InvalidPurchaseException e) {
			logger.info((e.getMessage()));
			assert (e.getMessage().startsWith(ErrorMessage.SEAT_RESERVATION_UNEXPECTED_ERROR.getMessage()));
		} catch (RuntimeException e) {
			fail(e.getMessage());
		}
	}

	private TicketTypeRequest getTicketTypeRequestsCase1() {
		TicketTypeRequest request = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
		return request;
	}

	private TicketPurchaseExpectedValues getTicketPurchaseExpectedValuesCase1() {
		TicketPurchaseExpectedValues expectedValues = new TicketPurchaseExpectedValues();
		expectedValues.thereIsAnAdult = true;
		expectedValues.totalTickets = 2;
		expectedValues.totalAmountToPay = 2 * TicketPrice.getTicketPrice(TicketTypeRequest.Type.ADULT);
		expectedValues.totalSeatsToAllocate = 2;

		return expectedValues;
	}

	private TicketTypeRequest[] getTicketTypeRequestsCase2() {
		TicketTypeRequest[] result = new TicketTypeRequest[] { 
				new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
				new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3),
				new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1) 
		};
		return result;
	}

	private TicketPurchaseExpectedValues getTicketPurchaseExpectedValuesCase2() {
		TicketPurchaseExpectedValues expectedValues = new TicketPurchaseExpectedValues();
		expectedValues.thereIsAnAdult = true;
		expectedValues.totalTickets = 6;
		expectedValues.totalAmountToPay = 2 * TicketPrice.getTicketPrice(TicketTypeRequest.Type.ADULT)
				+ 3 * TicketPrice.getTicketPrice(TicketTypeRequest.Type.CHILD);

		expectedValues.totalSeatsToAllocate = 5;
		return expectedValues;
	}

	private TicketPurchaseExpectedValues getTicketPurchaseExpectedValuesCase3() {
		TicketPurchaseExpectedValues expectedValues = new TicketPurchaseExpectedValues();
		expectedValues.thereIsAnAdult = false;
		expectedValues.totalTickets = 0;
		expectedValues.totalAmountToPay = 0;
		expectedValues.totalSeatsToAllocate = 0;
		return expectedValues;
	}

	private TicketTypeRequest[] getTicketTypeRequestsCase4() {
		TicketTypeRequest[] result = new TicketTypeRequest[] { 
				new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0),
				new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 0),
				new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 0) 
		};

		return result;
	}

	private TicketPurchaseExpectedValues getTicketPurchaseExpectedValuesCase4() {

		TicketPurchaseExpectedValues expectedValues = new TicketPurchaseExpectedValues();
		expectedValues.thereIsAnAdult = true;
		expectedValues.totalTickets = 0;
		expectedValues.totalAmountToPay = 0 * TicketPrice.getTicketPrice(TicketTypeRequest.Type.ADULT)
				+ 0 * TicketPrice.getTicketPrice(TicketTypeRequest.Type.CHILD);

		expectedValues.totalSeatsToAllocate = 0;
		return expectedValues;
	}

	private TicketTypeRequest[] getTicketTypeRequestsExceptionAdultMissing() {
		TicketTypeRequest[] result = new TicketTypeRequest[] { 
				new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3),
				new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1) 
		};

		return result;
	}

	private TicketTypeRequest[] getTicketTypeRequestsExceptionMaxTickets() {
		TicketTypeRequest[] result = new TicketTypeRequest[] { 
				new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
				new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10),
				new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 11) 
		};

		return result;
	}

	private Long getAccountId() {
		return Long.valueOf(1);
	}

	private boolean isValidTicketPurchase(TicketService ticketService, TicketPurchaseExpectedValues expectedValues) {
		boolean result = false;
		try {
			Field totalAmountToPay = TicketServiceImpl.class.getDeclaredField("totalAmountToPay");
			totalAmountToPay.setAccessible(true);
			int totalAmountToPayValue = totalAmountToPay.getInt(ticketService);

			Field totalSeatsToAllocate = TicketServiceImpl.class.getDeclaredField("totalSeatsToAllocate");
			totalSeatsToAllocate.setAccessible(true);
			int totalSeatsToAllocateValue = totalSeatsToAllocate.getInt(ticketService);

			Field thereIsAnAdult = TicketServiceImpl.class.getDeclaredField("thereIsAnAdult");
			thereIsAnAdult.setAccessible(true);
			boolean thereIsAnAdultValue = thereIsAnAdult.getBoolean(ticketService);

			Field totalTickets = TicketServiceImpl.class.getDeclaredField("totalTickets");
			totalTickets.setAccessible(true);
			int totalTicketsValue = totalTickets.getInt(ticketService);

			result = (totalAmountToPayValue == expectedValues.totalAmountToPay)
					&& (totalSeatsToAllocateValue == expectedValues.totalSeatsToAllocate)
					&& (thereIsAnAdultValue == expectedValues.thereIsAnAdult)
					&& (totalTicketsValue == expectedValues.totalTickets)
					&& ((totalTicketsValue == 0) || (thereIsAnAdultValue));

		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

}
