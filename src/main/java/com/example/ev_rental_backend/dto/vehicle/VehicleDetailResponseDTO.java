package com.example.ev_rental_backend.dto.vehicle;

import com.example.ev_rental_backend.dto.booking.BookingHistoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDetailResponseDTO {
    private Long vehicleId;
    private String vehicleName;
    private String plateNumber;
    private String status;
    private String description;
    private String modelName;
    private String stationName;
    private Double pricePerHour;
    private Double pricePerDay;
    private Double batteryLevel;
    private Double mileage;

    private List<BookingHistoryDTO> bookingHistory;
    private List<VehicleFeedbackDTO> feedbacks;

    private List<String> imageUrls;
}
