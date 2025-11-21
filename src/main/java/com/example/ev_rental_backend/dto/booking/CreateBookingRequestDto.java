package com.example.ev_rental_backend.dto.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequestDto {

    @NotNull(message = "Vui lòng chọn xe cần thuê")
    private Long vehicleId;

    @NotNull(message = "Vui lòng chọn thời gian bắt đầu thuê")
    @Future(message = "Thời gian bắt đầu phải nằm trong tương lai")
    private LocalDateTime startDateTime;

    @NotNull(message = "Vui lòng chọn thời gian kết thúc thuê")
    @Future(message = "Thời gian kết thúc phải nằm trong tương lai")
    private LocalDateTime endDateTime;
}
