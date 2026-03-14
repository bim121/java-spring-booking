package org.example.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.model.AccommodationType;
import org.example.model.Address;
import org.example.model.BookingStatus;
import org.example.model.PaymentStatus;
import org.example.model.UserRole;
import org.example.repository.AccommodationRepository;
import org.example.repository.BookingRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtTokenProvider;

public class TestDataFactory {
    private static final String DEFAULT_PASSWORD = "$2a$10$dummyHashForTestingPurposesOnly";

    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TestDataFactory(
            UserRepository userRepository,
            AccommodationRepository accommodationRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.accommodationRepository = accommodationRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public User createUser(String email, String firstName, String lastName, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(DEFAULT_PASSWORD);
        user.setRole(role);
        return userRepository.save(user);
    }

    public User createCustomer(String email) {
        return createUser(email, "Customer", "User", UserRole.CUSTOMER);
    }

    public User createManager(String email) {
        return createUser(email, "Manager", "User", UserRole.MANAGER);
    }

    public User createAdmin(String email) {
        return createUser(email, "Admin", "User", UserRole.ADMIN);
    }

    public Accommodation createAccommodation(
            AccommodationType type,
            String size,
            BigDecimal dailyRate,
            int availability) {
        Accommodation accommodation = new Accommodation();
        accommodation.setType(type);
        accommodation.setLocation(new Address("123 Main St", "City", "State", "12345", "Country"));
        accommodation.setSize(size);
        accommodation.setDailyRate(dailyRate);
        accommodation.setAvailability(availability);
        return accommodationRepository.save(accommodation);
    }

    public Accommodation createDefaultAccommodation() {
        return createAccommodation(
                AccommodationType.HOUSE,
                "100 sqm",
                new BigDecimal("50.00"),
                2);
    }

    public Booking createBooking(
            User user,
            Accommodation accommodation,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public Booking createPendingBooking(User user, Accommodation accommodation) {
        return createBooking(
                user,
                accommodation,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                BookingStatus.PENDING);
    }

    public Payment createPayment(
            Booking booking,
            BigDecimal amount,
            PaymentStatus status,
            String sessionId) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmountToPay(amount);
        payment.setStatus(status);
        payment.setSessionId(sessionId);
        return paymentRepository.save(payment);
    }

    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(
                user.getEmail(), user.getId(), user.getRole().name());
    }

    public void clearAll() {
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        accommodationRepository.deleteAll();
        userRepository.deleteAll();
    }
}
