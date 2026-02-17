package org.example.mapper;

import java.util.List;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {
    BookingDetailResponse toDetailResponse(Booking booking);

    List<BookingDetailResponse> toDetailResponseList(List<Booking> bookings);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toBooking(BookingRequest request);

    default BookingDetailResponse.AccommodationInfo mapAccommodation(
            org.example.entity.Accommodation accommodation) {
        if (accommodation == null) {
            return null;
        }
        return new BookingDetailResponse.AccommodationInfo(
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getLocation(),
                accommodation.getSize(),
                accommodation.getAmenities(),
                accommodation.getDailyRate(),
                accommodation.getAvailability()
        );
    }

    default BookingDetailResponse.UserInfo mapUser(org.example.entity.User user) {
        if (user == null) {
            return null;
        }
        return new BookingDetailResponse.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
