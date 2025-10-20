package com.example.ev_rental_backend.config.util;

import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.entity.VehicleModel;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.repository.StationRepository;
import com.example.ev_rental_backend.repository.VehicleModelRepository;
import com.example.ev_rental_backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataUtil implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

    }
}
