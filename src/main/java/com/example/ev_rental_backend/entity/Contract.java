package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "contract")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contractId;

    @OneToOne @JoinColumn(name = "booking_id")
    private Booking booking;

    private LocalDate contractDate;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TermCondition> termConditions;

    private String renterSignature;
    private String staffSignature;

    public enum ContractType { ELECTRONIC, PAPER }

    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL)
    private OtpVerification otpVerification;
}
