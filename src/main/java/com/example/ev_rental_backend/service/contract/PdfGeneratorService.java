package com.example.ev_rental_backend.service.contract;

import com.example.ev_rental_backend.entity.Contract;
import com.example.ev_rental_backend.entity.TermCondition;
import com.example.ev_rental_backend.repository.TermConditionRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.pdf.BaseFont;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final Configuration freemarkerConfig;
    private final TermConditionRepository termConditionRepository;

    public String generateContractFile(Contract contract) {
        try {
            // 🧾 1️⃣ Chuẩn bị dữ liệu cho FreeMarker template
            Map<String, Object> data = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Thông tin cơ bản của hợp đồng
            data.put("companyName", "EV Rental");
            data.put("contractDate", contract.getContractDate() != null
                    ? contract.getContractDate().format(formatter)
                    : LocalDateTime.now().format(formatter));
            data.put("contractStatus", contract.getStatus().name());

            // --- Người thuê ---
            var renter = contract.getBooking().getRenter();
            String renterFullName = renter.getIdentityDocuments().stream()
                    .filter(doc -> doc.getStatus() == com.example.ev_rental_backend.entity.IdentityDocument.DocumentStatus.VERIFIED)
                    .filter(doc -> doc.getType() == com.example.ev_rental_backend.entity.IdentityDocument.DocumentType.NATIONAL_ID
                            || doc.getType() == com.example.ev_rental_backend.entity.IdentityDocument.DocumentType.DRIVER_LICENSE)
                    .map(com.example.ev_rental_backend.entity.IdentityDocument::getFullName)
                    .findFirst()
                    .orElse(renter.getFullName());
            data.put("renterName", renterFullName);
            data.put("renterEmail", renter.getEmail());
            data.put("renterPhone", renter.getPhoneNumber());

            // --- Nhân viên & Xe ---
            data.put("staffName", contract.getBooking().getStaff().getFullName());
            data.put("vehicleName", contract.getBooking().getVehicle().getVehicleName());
            data.put("vehiclePlate", contract.getBooking().getVehicle().getPlateNumber());

            // --- Thời gian thuê ---
            data.put("startDate", contract.getBooking().getStartDateTime() != null
                    ? Date.from(contract.getBooking().getStartDateTime().atZone(ZoneId.systemDefault()).toInstant())
                    : null);

            data.put("endDate", contract.getBooking().getEndDateTime() != null
                    ? Date.from(contract.getBooking().getEndDateTime().atZone(ZoneId.systemDefault()).toInstant())
                    : null);

            // --- Giá ---
            data.put("pricePerHour", contract.getBooking().getPriceSnapshotPerHour());
            data.put("pricePerDay", contract.getBooking().getPriceSnapshotPerDay());

            // --- Admin ---
            data.put("adminName", (contract.getAdmin() != null) ? contract.getAdmin().getFullName() : "");
            data.put("adminSignedAt", (contract.getAdminSignedAt() != null)
                    ? contract.getAdminSignedAt().format(formatter)
                    : "");
            data.put("renterSignedAt", (contract.getRenterSignedAt() != null)
                    ? contract.getRenterSignedAt().format(formatter)
                    : "");

            // --- Điều khoản ---
            List<TermCondition> terms = contract.getTerms();
            if (terms == null || terms.isEmpty()) {
                terms = termConditionRepository.findByContract(contract);
            }
            data.put("terms", terms != null ? terms : new ArrayList<>());

            // 🧩 2️⃣ Load FreeMarker template
            Template template = freemarkerConfig.getTemplate("contract_template.html");

            // 3️⃣ Render dữ liệu sang HTML
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            String htmlContent = writer.toString();

            // 📁 4️⃣ Tạo thư mục lưu nếu chưa tồn tại
            Path outputPath = Paths.get("uploads/contracts/");
            Files.createDirectories(outputPath);

            // 📄 5️⃣ Tạo file PDF
            String fileName = "contract_" + contract.getContractId() + ".pdf";
            Path pdfFile = outputPath.resolve(fileName);

            try (OutputStream os = new FileOutputStream(pdfFile.toFile())) {
                ITextRenderer renderer = new ITextRenderer();

                // ✅ Thêm font Unicode hỗ trợ tiếng Việt
                String fontPath = "src/main/resources/fonts/DejaVuSans.ttf";
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                // ✅ Render PDF
                String baseUrl = new File("uploads/contracts/").toURI().toURL().toString();
                renderer.setDocumentFromString(htmlContent, baseUrl);
                renderer.layout();
                renderer.createPDF(os);
                renderer.finishPDF();
            }

            // 🌐 6️⃣ Trả về URL truy cập (mapping qua /files/contracts/**)
            return "http://localhost:8080/files/contracts/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi render hợp đồng: " + e.getMessage(), e);
        }
    }

}
