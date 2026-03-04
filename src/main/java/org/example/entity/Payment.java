package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.net.URL;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.model.PaymentStatus;

@Entity
@Table(name = "payments")
@NamedEntityGraph(
        name = "Payment.withBookingUserAccommodation",
        attributeNodes = {
                @NamedAttributeNode(value = "booking", subgraph = "bookingSubgraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "bookingSubgraph",
                        attributeNodes = {
                                @NamedAttributeNode("user"),
                                @NamedAttributeNode("accommodation")
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"booking"})
@ToString(exclude = {"booking"})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "session_url")
    private URL sessionUrl;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "amount_to_pay", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountToPay;
}
