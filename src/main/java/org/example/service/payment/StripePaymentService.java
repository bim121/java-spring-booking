package org.example.service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;

public interface StripePaymentService {
    Session createCheckoutSession(SessionCreateParams params) throws StripeException;

    Session retrieveSession(String sessionId) throws StripeException;

    SessionCreateParams.LineItem createLineItem(String name, String description, BigDecimal amount);

    SessionCreateParams createSessionParams(
            String successUrl, String cancelUrl, SessionCreateParams.LineItem lineItem);

    URL buildSessionUrl(String urlString) throws MalformedURLException;
}
