package org.example.mapper;

import java.util.List;
import org.example.dto.response.PaymentDetailResponse;
import org.example.dto.response.PaymentResponse;
import org.example.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {
    @Mapping(target = "bookingId", source = "booking.id")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);

    PaymentDetailResponse toDetailResponse(Payment payment);

    default PaymentDetailResponse.BookingInfo mapBooking(org.example.entity.Booking booking) {
        if (booking == null) {
            return null;
        }
        return new PaymentDetailResponse.BookingInfo(
                booking.getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getStatus() != null ? booking.getStatus().name() : null,
                mapAccommodation(booking.getAccommodation()),
                mapUser(booking.getUser())
        );
    }

    default PaymentDetailResponse.AccommodationInfo mapAccommodation(
            org.example.entity.Accommodation accommodation) {
        if (accommodation == null) {
            return null;
        }
        return new PaymentDetailResponse.AccommodationInfo(
                accommodation.getId(),
                accommodation.getType() != null ? accommodation.getType().name() : null,
                accommodation.getLocation(),
                accommodation.getSize(),
                accommodation.getAmenities(),
                accommodation.getDailyRate(),
                accommodation.getAvailability()
        );
    }

    default PaymentDetailResponse.UserInfo mapUser(org.example.entity.User user) {
        if (user == null) {
            return null;
        }
        return new PaymentDetailResponse.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
