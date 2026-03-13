package org.example.service.payment.integration;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import org.example.service.payment.StripePaymentService;
import org.springframework.test.util.ReflectionTestUtils;

class FakeStripePaymentService implements StripePaymentService {
    @Override
    public Session createCheckoutSession(SessionCreateParams params) throws StripeException {
        Session session = new Session();
        ReflectionTestUtils.setField(session, "id", "cs_test_123");
        ReflectionTestUtils.setField(session, "url", "https://checkout.stripe.com/pay/cs_test_123");
        return session;
    }

    @Override
    public Session retrieveSession(String sessionId) throws StripeException {
        Session session = new Session();
        ReflectionTestUtils.setField(session, "id", sessionId);
        ReflectionTestUtils.setField(session, "paymentStatus", "paid");
        return session;
    }

    @Override
    public SessionCreateParams.LineItem createLineItem(String name, String description,
                                                       BigDecimal amount) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount.multiply(new BigDecimal("100")).longValue())
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(name)
                                                .setDescription(description)
                                                .build())
                                .build())
                .build();
    }

    @Override
    public SessionCreateParams createSessionParams(
            String successUrl, String cancelUrl, SessionCreateParams.LineItem lineItem) {
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .build();
    }

    @Override
    public URL buildSessionUrl(String urlString) throws MalformedURLException {
        return new URL(urlString);
    }
}
