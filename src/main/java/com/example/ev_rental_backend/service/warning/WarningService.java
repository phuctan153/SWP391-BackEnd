package com.example.ev_rental_backend.service.warning;

import com.example.ev_rental_backend.dto.warning.WarningRequestDTO;
import com.example.ev_rental_backend.dto.warning.WarningResponseDTO;

public interface WarningService {
    public WarningResponseDTO sendWarningEmail(WarningRequestDTO dto);


}
