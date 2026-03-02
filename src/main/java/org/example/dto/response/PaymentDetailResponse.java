package org.example.dto.response;

import java.math.BigDecimal;
import java.net.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Address;
import org.example.model.PaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponse {
    private Long id;
    private PaymentStatus status;
    private BigDecimal amountToPay;
    private URL sessionUrl;
    private String sessionId;
    private BookingInfo booking;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingInfo {
        private Long id;
        private java.time.LocalDate checkInDate;
        private java.time.LocalDate checkOutDate;
        private String status;
        private AccommodationInfo accommodation;
        private UserInfo user;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccommodationInfo {
        private Long id;
        private String type;
        private Address location;
        private String size;
        private java.util.List<String> amenities;
        private BigDecimal dailyRate;
        private Integer availability;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
    }
}
