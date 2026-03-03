package org.example.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.stripe.Stripe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class StripeConfigTest {
    private StripeConfig stripeConfig;

    @BeforeEach
    void setUp() {
        stripeConfig = new StripeConfig();
        ReflectionTestUtils.setField(stripeConfig, "stripeApiKey", "sk_test_test_key");
    }

    @Test
    void init_setsStripeApiKey() {
        stripeConfig.init();
        assertNotNull(Stripe.apiKey);
    }
}
