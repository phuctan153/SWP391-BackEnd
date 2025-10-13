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

    private final RenterRepository renterRepository;
    private final StationRepository stationRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleRepository vehicleRepository;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting DataUtil seeding...");

        // 🔹 1️⃣ Seed Renter
        if (renterRepository.count() == 0) {
            Renter renter = Renter.builder()
                    .fullName("Nguyen Van A")
                    .email("nguyenvana@example.com")
                    .password("123456")
                    .phoneNumber("0987654321")
                    .nationalId("123456789")
                    .driverLicense("DL123456")
                    .driverLicenseExpiry(LocalDate.now().plusYears(3))
                    .dateOfBirth(LocalDate.of(1999, 5, 10))
                    .address("123 Le Loi, Ho Chi Minh City")
                    .status(Renter.Status.VERIFIED)
                    .isBlacklisted(false)
                    .authProvider(Renter.AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            renterRepository.save(renter);
            System.out.println("✅ Inserted test Renter");
        }

        // 🔹 2️⃣ Seed Station
        if (stationRepository.count() == 0) {
            Station station = Station.builder()
                    .name("EV Central Station")
                    .location("District 1, Ho Chi Minh City")
                    .latitude(10.7769)
                    .longitude(106.7009)
                    .car_number(10)
                    .status(Station.Status.ACTIVE)
                    .build();
            stationRepository.save(station);
            System.out.println("✅ Inserted test Station");
        }

        // 🔹 3️⃣ Seed VehicleModel
        if (vehicleModelRepository.count() == 0) {
            VehicleModel model = VehicleModel.builder()
                    .modelName("VinFast VF e34")
                    .manufacturer("VinFast")
                    .batteryCapacity(42.0)
                    .seatingCapacity(5)
                    .build();
            vehicleModelRepository.save(model);
            System.out.println("✅ Inserted test VehicleModel");
        }

        // 🔹 4️⃣ Seed Vehicle
        if (vehicleRepository.count() == 0) {
            Station station = stationRepository.findAll().get(0);
            VehicleModel model = vehicleModelRepository.findAll().get(0);

            Vehicle vehicle = Vehicle.builder()
                    .vehicleName("VF e34 - A12345")
                    .station(station)
                    .model(model)
                    .pricePerHour(100000.0)
                    .pricePerDay(600000.0)
                    .plateNumber("59A1-12345")
                    .batteryLevel(85.0)
                    .mileage(15000.0)
                    .description("VinFast VF e34 in great condition.")
                    .status(Vehicle.Status.AVAILABLE)
                    .build();

            vehicleRepository.save(vehicle);
            System.out.println("✅ Inserted test Vehicle");
        }

        System.out.println("🎉 DataUtil seeding completed successfully!");
    }
}
