package org.example.service.booking.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.User;
import org.example.model.AccommodationType;
import org.example.model.Address;
import org.example.model.BookingStatus;
import org.example.model.UserRole;
import org.example.repository.AccommodationRepository;
import org.example.repository.BookingRepository;
import org.example.repository.UserRepository;
import org.example.service.booking.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Accommodation testAccommodation;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        accommodationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("$2a$10$dummyHashForTestingPurposesOnly");
        testUser.setRole(UserRole.CUSTOMER);
        testUser = userRepository.save(testUser);

        testAccommodation = new Accommodation();
        testAccommodation.setType(AccommodationType.HOUSE);
        testAccommodation.setLocation(new Address("123 Main St", "City",
                "State", "12345", "Country"));
        testAccommodation.setSize("100 sqm");
        testAccommodation.setDailyRate(new BigDecimal("50.00"));
        testAccommodation.setAvailability(2);
        testAccommodation = accommodationRepository.save(testAccommodation);
    }

    @Test
    void createBooking_success_createsBookingInDatabase() {
        BookingRequest request = new BookingRequest();
        request.setAccommodationId(testAccommodation.getId());
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        BookingDetailResponse response = bookingService.createBooking(request, testUser.getEmail());

        assertNotNull(response);
        assertEquals(testAccommodation.getId(),
                response.getAccommodation().getId());
        assertEquals(testUser.getId(), response.getUser().getId());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        assertEquals(request.getCheckInDate(), response.getCheckInDate());
        assertEquals(request.getCheckOutDate(), response.getCheckOutDate());

        Booking savedBooking = bookingRepository.findById(response.getId())
                .orElseThrow();
        assertEquals(testAccommodation.getId(), savedBooking.getAccommodation().getId());
        assertEquals(testUser.getId(), savedBooking.getUser().getId());
        assertEquals(BookingStatus.PENDING, savedBooking.getStatus());
    }

    @Test
    void createBooking_whenAccommodationNotAvailable_throwsException() {
        Accommodation fullAccommodation = new Accommodation();
        fullAccommodation.setType(AccommodationType.HOUSE);
        fullAccommodation.setLocation(new Address("456 St", "City",
                "State", "12345", "Country"));
        fullAccommodation.setSize("50 sqm");
        fullAccommodation.setDailyRate(new BigDecimal("30.00"));
        fullAccommodation.setAvailability(1);
        fullAccommodation = accommodationRepository.save(fullAccommodation);

        BookingRequest request1 = new BookingRequest();
        request1.setAccommodationId(fullAccommodation.getId());
        request1.setCheckInDate(LocalDate.now().plusDays(1));
        request1.setCheckOutDate(LocalDate.now().plusDays(2));
        bookingService.createBooking(request1, testUser.getEmail());

        BookingRequest request2 = new BookingRequest();
        request2.setAccommodationId(fullAccommodation.getId());
        request2.setCheckInDate(LocalDate.now().plusDays(1));
        request2.setCheckOutDate(LocalDate.now().plusDays(2));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request2, testUser.getEmail()));
    }

    @Test
    void getUserBookings_returnsOnlyUserBookings() {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setPassword("$2a$10$dummyHash");
        otherUser.setRole(UserRole.CUSTOMER);
        otherUser = userRepository.save(otherUser);

        BookingRequest request1 = new BookingRequest();
        request1.setAccommodationId(testAccommodation.getId());
        request1.setCheckInDate(LocalDate.now().plusDays(1));
        request1.setCheckOutDate(LocalDate.now().plusDays(2));
        bookingService.createBooking(request1, testUser.getEmail());

        BookingRequest request2 = new BookingRequest();
        request2.setAccommodationId(testAccommodation.getId());
        request2.setCheckInDate(LocalDate.now().plusDays(5));
        request2.setCheckOutDate(LocalDate.now().plusDays(6));
        bookingService.createBooking(request2, otherUser.getEmail());

        var userBookings = bookingService.getUserBookings(
                testUser.getEmail(), null, PageRequest.of(0, 10));

        assertEquals(1, userBookings.getTotalElements());
        assertEquals(testUser.getId(), userBookings.getContent().get(0).getUser().getId());
    }
}
