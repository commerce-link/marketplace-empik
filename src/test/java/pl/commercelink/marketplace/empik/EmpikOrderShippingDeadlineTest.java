package pl.commercelink.marketplace.empik;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmpikOrderShippingDeadlineTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private EmpikOrder orderWithDeadline(String shippingDeadline) throws Exception {
        String value = shippingDeadline == null ? "null" : "\"" + shippingDeadline + "\"";
        return MAPPER.readValue("{\"shipping_deadline\": " + value + "}", EmpikOrder.class);
    }

    @Test
    @DisplayName("ISO instant deadline becomes the estimated shipping date")
    void isoInstantIsParsed() throws Exception {
        EmpikOrder order = orderWithDeadline("2026-09-03T14:30:00Z");

        assertEquals(LocalDate.of(2026, 9, 3), order.toEstimatedShippingAt());
    }

    @Test
    @DisplayName("plain date deadline becomes the estimated shipping date")
    void plainDateIsParsed() throws Exception {
        EmpikOrder order = orderWithDeadline("2026-09-03");

        assertEquals(LocalDate.of(2026, 9, 3), order.toEstimatedShippingAt());
    }

    @Test
    @DisplayName("missing deadline yields no estimated shipping date")
    void missingDeadlineIsNull() throws Exception {
        assertNull(orderWithDeadline(null).toEstimatedShippingAt());
        assertNull(orderWithDeadline("").toEstimatedShippingAt());
        assertNull(MAPPER.readValue("{}", EmpikOrder.class).toEstimatedShippingAt());
    }

    @Test
    @DisplayName("unparsable deadline does not break the import")
    void unparsableDeadlineIsNull() throws Exception {
        assertNull(orderWithDeadline("not-a-date").toEstimatedShippingAt());
    }
}
