package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.client.MomoApi;
import com.example.ev_rental_backend.dto.payment.CreateMomoRequest;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import com.example.ev_rental_backend.dto.payment.MomoIPNRequest;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Invoice;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoService {

    private final MomoApi momoApi;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentService paymentService;

    @Value(value = "${momo.partner-code}")
    private String PARTNER_CODE;

    @Value(value = "${momo.access-key}")
    private String ACCESS_KEY;

    @Value(value = "${momo.secret-key}")
    private String SECRET_KEY;

    @Value(value = "${momo.return-url}")
    private String REDIRECT_URL;

    @Value(value = "${momo.ipn-url}")
    private String IPN_URL;

    @Value(value = "${momo.request-type}")
    private String REQUEST_TYPE;

}
