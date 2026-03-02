package org.example.service.notification.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.model.Address;
import org.example.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TelegramNotificationService implements NotificationService {
    private static final String SEND_MESSAGE_ENDPOINT = "/sendMessage";
    private final WebClient webClient;
    private final String botToken;
    private final String chatId;
    private final String apiUrl;

    public TelegramNotificationService(
            WebClient webClient,
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.chat-id}") String chatId,
            @Value("${telegram.bot.api-url}") String apiUrl) {
        this.webClient = webClient;
        this.botToken = botToken;
        this.chatId = chatId;
        this.apiUrl = apiUrl;

        log.info("Initializing TelegramNotificationService with chatId: '{}', botToken: '{}'",
                chatId != null ? chatId : "NULL",
                botToken != null && !botToken.isBlank() ? "***configured***" : "NOT SET");

        if (botToken == null || botToken.isBlank() || botToken.equals("your_telegram_bot_token")) {
            log.warn("TELEGRAM_BOT_TOKEN is not configured properly. Notifications will fail.");
        }
        if (chatId == null || chatId.isBlank() || chatId.equals("your_telegram_chat_id")) {
            log.error("TELEGRAM_CHAT_ID is not configured! Current value: '{}'. "
                    + "Please set TELEGRAM_CHAT_ID in .env file.", chatId);
        } else {
            log.info("Telegram notification service initialized successfully. Chat ID: '{}'",
                    chatId);
        }
    }

    @Override
    public void notifyBookingCreated(Booking booking) {
        String message = String.format(
                "*New Booking Created*%n%n"
                        + "*Booking ID:* %d%n"
                        + "*User:* %s %s (%s)%n"
                        + "*Accommodation:* %s - %s%n"
                        + "*Location:* %s%n"
                        + "*Check-in:* %s%n"
                        + "*Check-out:* %s%n"
                        + "*Daily Rate:* $%.2f%n"
                        + "*Status:* %s",
                booking.getId(),
                booking.getUser().getFirstName(),
                booking.getUser().getLastName(),
                booking.getUser().getEmail(),
                booking.getAccommodation().getType(),
                booking.getAccommodation().getSize(),
                formatAddress(booking.getAccommodation().getLocation()),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getAccommodation().getDailyRate(),
                booking.getStatus()
        );
        sendMessage(message);
    }

    @Override
    public void notifyBookingCanceled(Booking booking) {
        String message = String.format(
                "*Booking Canceled*%n%n"
                        + "*Booking ID:* %d%n"
                        + "*User:* %s %s (%s)%n"
                        + "*Accommodation:* %s - %s%n"
                        + "*Location:* %s%n"
                        + "*Check-in:* %s%n"
                        + "*Check-out:* %s%n"
                        + "*Status:* %s",
                booking.getId(),
                booking.getUser().getFirstName(),
                booking.getUser().getLastName(),
                booking.getUser().getEmail(),
                booking.getAccommodation().getType(),
                booking.getAccommodation().getSize(),
                formatAddress(booking.getAccommodation().getLocation()),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getStatus()
        );
        sendMessage(message);
    }

    @Override
    public void notifyAccommodationCreated(Accommodation accommodation) {
        String message = String.format(
                "*New Accommodation Created*%n%n"
                        + "*Accommodation ID:* %d%n"
                        + "*Type:* %s%n"
                        + "*Size:* %s%n"
                        + "*Location:* %s%n"
                        + "*Daily Rate:* $%.2f%n"
                        + "*Availability:* %d units%n"
                        + "*Amenities:* %s",
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getSize(),
                formatAddress(accommodation.getLocation()),
                accommodation.getDailyRate(),
                accommodation.getAvailability(),
                accommodation.getAmenities() != null && !accommodation.getAmenities().isEmpty()
                        ? String.join(", ", accommodation.getAmenities())
                        : "None"
        );
        sendMessage(message);
    }

    @Override
    public void notifyAccommodationReleased(Accommodation accommodation) {
        String message = String.format(
                "*Accommodation Released*%n%n"
                        + "*Accommodation ID:* %d%n"
                        + "*Type:* %s%n"
                        + "*Size:* %s%n"
                        + "*Location:* %s%n"
                        + "*Availability:* %d units",
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getSize(),
                formatAddress(accommodation.getLocation()),
                accommodation.getAvailability()
        );
        sendMessage(message);
    }

    @Override
    public void notifyPaymentSuccessful(Payment payment) {
        String message = String.format(
                "*Payment Successful*%n%n"
                        + "*Payment ID:* %d%n"
                        + "*Booking ID:* %d%n"
                        + "*Amount:* $%.2f%n"
                        + "*Status:* %s%n"
                        + "*Session ID:* %s",
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmountToPay(),
                payment.getStatus(),
                payment.getSessionId()
        );
        sendMessage(message);
    }

    private void sendMessage(String text) {
        try {
            if (chatId == null || chatId.isBlank() || chatId.equals("your_telegram_chat_id")) {
                log.error("Cannot send Telegram notification: TELEGRAM_CHAT_ID is not configured. "
                        + "chatId value: '{}', isNull: {}, isBlank: {}",
                        chatId, chatId == null, chatId != null && chatId.isBlank());
                return;
            }
            if (botToken == null
                    || botToken.isBlank()
                    || botToken.equals("your_telegram_bot_token")) {
                log.error("Cannot send Telegram notification: TELEGRAM_BOT_TOKEN "
                        + "is not configured");
                return;
            }
            String url = apiUrl + botToken + SEND_MESSAGE_ENDPOINT;
            TelegramMessageRequest request = new TelegramMessageRequest(
                    chatId,
                    text,
                    "Markdown"
            );
            webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Telegram API error: {}", errorBody);
                                        return Mono.error(new RuntimeException(errorBody));
                                    })
                    )
                    .bodyToMono(TelegramMessageResponse.class)
                    .block();
            log.debug("Telegram notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send Telegram notification: {}", e.getMessage(), e);
        }
    }

    private String formatAddress(Address address) {
        if (address == null) {
            return "N/A";
        }
        StringBuilder sb = new StringBuilder();
        if (address.getStreet() != null) {
            sb.append(address.getStreet());
        }
        if (address.getCity() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getCity());
        }
        if (address.getState() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getState());
        }
        if (address.getZipCode() != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(address.getZipCode());
        }
        if (address.getCountry() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getCountry());
        }
        return sb.length() > 0 ? sb.toString() : "N/A";
    }

    private record TelegramMessageRequest(
            @JsonProperty("chat_id") String chatId,
            String text,
            @JsonProperty("parse_mode") String parseMode) {
    }

    private record TelegramMessageResponse(boolean ok, String description) {
    }
}
