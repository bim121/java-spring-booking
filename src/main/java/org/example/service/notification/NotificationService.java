package org.example.service.notification;

import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.Payment;

public interface NotificationService {
    void notifyBookingCreated(Booking booking);

    void notifyBookingCanceled(Booking booking);

    void notifyAccommodationCreated(Accommodation accommodation);

    void notifyAccommodationReleased(Accommodation accommodation);

    void notifyPaymentSuccessful(Payment payment);
}
