package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.model.AccommodationType;
import org.example.model.Address;
import org.example.model.BookingStatus;

@Data
@AllArgsConstructor
public class BookingDetailResponse {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;
    private AccommodationInfo accommodation;
    private UserInfo user;

    @Data
    @AllArgsConstructor
    public static class AccommodationInfo {
        private Long id;
        private AccommodationType type;
        private Address location;
        private String size;
        private List<String> amenities;
        private BigDecimal dailyRate;
        private Integer availability;
    }

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
    }
}
