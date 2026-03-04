package org.example.service.payment.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.example.service.payment.StripePaymentService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripePaymentServiceImpl implements StripePaymentService {
    @Override
    public Session createCheckoutSession(SessionCreateParams params) throws StripeException {
        return Session.create(params);
    }

    @Override
    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }

    @Override
    public SessionCreateParams.LineItem createLineItem(
            String name, String description, BigDecimal amount) {
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(name)
                        .setDescription(description)
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                        .setProductData(productData)
                        .build();

        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceData)
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
