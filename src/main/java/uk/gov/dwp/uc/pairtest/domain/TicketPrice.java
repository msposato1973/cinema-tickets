package uk.gov.dwp.uc.pairtest.domain;

import java.util.HashMap;
import java.util.Map;

public class TicketPrice {

	 private static Map<TicketTypeRequest.Type,Integer> ticketPriceMap =
	            new HashMap<>();

	    static {
	        ticketPriceMap.put(TicketTypeRequest.Type.ADULT,20);
	        ticketPriceMap.put(TicketTypeRequest.Type.CHILD,10);
	        ticketPriceMap.put(TicketTypeRequest.Type.INFANT,0);
	    }

	    public static int getTicketPrice(TicketTypeRequest.Type ticketType) {
	        return ticketPriceMap.get(ticketType);
	    }
}
