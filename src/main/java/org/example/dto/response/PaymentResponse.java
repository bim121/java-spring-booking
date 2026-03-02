package org.example.dto.response;

import java.math.BigDecimal;
import java.net.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.PaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private PaymentStatus status;
    private Long bookingId;
    private BigDecimal amountToPay;
    private URL sessionUrl;
    private String sessionId;
}
