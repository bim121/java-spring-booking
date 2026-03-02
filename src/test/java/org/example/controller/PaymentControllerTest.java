package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import org.example.dto.response.PaymentDetailResponse;
import org.example.dto.response.PaymentResponse;
import org.example.model.PaymentStatus;
import org.example.security.JwtTokenProvider;
import org.example.service.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getMyPayments_returnsOk() throws Exception {
        when(paymentService.getUserPayments(anyString(), any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(0, 20), 0));

        var auth = new UsernamePasswordAuthenticationToken(
                "u@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(get("/payments/my")
                        .param("page", "0")
                        .param("size", "20")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void createPaymentSession_returnsCreated() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L,
                PaymentStatus.PENDING,
                10L,
                new BigDecimal("30.00"),
                new URL("https://checkout.stripe.com/pay/cs_test"),
                "cs_test");
        when(paymentService.createPaymentSession(any(), anyString())).thenReturn(response);

        String body = """
                {
                  "bookingId": 10
                }
                """;

        var auth = new UsernamePasswordAuthenticationToken(
                "u@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(auth))
                .andExpect(status().isCreated());
    }

    @Test
    void getPaymentById_returnsOk() throws Exception {
        when(paymentService.getPaymentByIdForManager(anyLong()))
                .thenReturn(new PaymentDetailResponse(
                        1L,
                        PaymentStatus.PENDING,
                        new BigDecimal("30.00"),
                        null,
                        "cs_test",
                        null));

        var auth = new UsernamePasswordAuthenticationToken(
                "m@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

        mockMvc.perform(get("/payments/1")
                        .principal(auth))
                .andExpect(status().isOk());
    }
}

