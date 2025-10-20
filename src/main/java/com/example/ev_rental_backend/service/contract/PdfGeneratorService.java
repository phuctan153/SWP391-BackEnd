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
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final Configuration freemarkerConfig;
    private final TermConditionRepository termConditionRepository;

    public String generateContractFile(Contract contract) {
        try {
            // 🧾 Chuẩn bị dữ liệu cho template
            Map<String, Object> data = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            data.put("contractDate", contract.getContractDate().format(formatter));
            var renter = contract.getBooking().getRenter();
            String renterFullName = renter.getIdentityDocuments().stream()
                    .filter(doc -> doc.getStatus() == com.example.ev_rental_backend.entity.IdentityDocument.DocumentStatus.VERIFIED)
                    .filter(doc -> doc.getType() == com.example.ev_rental_backend.entity.IdentityDocument.DocumentType.NATIONAL_ID
                            || doc.getType() == com.example.ev_rental_backend.entity.IdentityDocument.DocumentType.DRIVER_LICENSE)
                    .map(com.example.ev_rental_backend.entity.IdentityDocument::getFullName)
                    .findFirst()
                    .orElse(renter.getFullName()); // fallback nếu chưa xác minh

            data.put("renterName", renterFullName);
            data.put("renterEmail", contract.getBooking().getRenter().getEmail());
            data.put("renterPhone", contract.getBooking().getRenter().getPhoneNumber());
            data.put("staffName", contract.getBooking().getStaff().getFullName());
            data.put("vehicleName", contract.getBooking().getVehicle().getVehicleName());
            data.put("vehiclePlate", contract.getBooking().getVehicle().getPlateNumber());
            data.put("startDate", contract.getBooking().getStartDateTime());
            data.put("endDate", contract.getBooking().getEndDateTime());
            data.put("pricePerHour", contract.getBooking().getPriceSnapshotPerHour());
            data.put("pricePerDay", contract.getBooking().getPriceSnapshotPerDay());
            data.put("contractStatus", contract.getStatus().name());
            data.put("adminName",
                    (contract.getAdmin() != null) ? contract.getAdmin().getFullName() : "");


            List<TermCondition> terms = contract.getTerms();
            if (terms == null || terms.isEmpty()) {
                terms = termConditionRepository.findByContract(contract);
            }
            data.put("terms", terms != null ? terms : new ArrayList<>());

            // 📄 Load template từ resources/templates
            Template template = freemarkerConfig.getTemplate("contract_template.html");

            // 🧩 Render dữ liệu sang HTML
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            String htmlContent = writer.toString();

            // 📁 Tạo thư mục output nếu chưa có
            Path outputPath = Paths.get("uploads/contracts/");
            Files.createDirectories(outputPath);

            String fileName = "contract_" + contract.getContractId() + ".pdf";
            Path pdfFile = outputPath.resolve(fileName);

            // 🖨️ Render PDF với font hỗ trợ Unicode tiếng Việt
            try (OutputStream os = new FileOutputStream(pdfFile.toFile())) {
                ITextRenderer renderer = new ITextRenderer();

                // ✅ Thêm font Unicode (Roboto hoặc Arial Unicode MS)
                String fontPath = "src/main/resources/fonts/DejaVuSans.ttf";
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                String baseUrl = new File("uploads/contracts/").toURI().toURL().toString();
                renderer.setDocumentFromString(htmlContent, baseUrl);
                renderer.layout();
                renderer.createPDF(os);
                renderer.finishPDF();
            }

            // 🌐 Trả về URL để frontend truy cập file
            return "http://localhost:8080/files/contracts/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi render hợp đồng: " + e.getMessage(), e);
        }
    }
}
