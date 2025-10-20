package com.example.ev_rental_backend.service.contract;

import com.example.ev_rental_backend.entity.TermCondition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TermTemplateService {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Đọc danh sách điều khoản mặc định từ file JSON
     */
    public List<TermCondition> loadDefaultTerms() {
        try {
            var resource = new ClassPathResource("templates/contract_terms.json");
            return mapper.readValue(resource.getInputStream(), new TypeReference<List<TermCondition>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Không thể load file mẫu điều khoản", e);
        }
    }
}
