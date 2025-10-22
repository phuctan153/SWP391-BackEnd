package com.example.ev_rental_backend.service.vehicle;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.entity.VehicleImage;
import com.example.ev_rental_backend.repository.VehicleImageRepository;
import com.example.ev_rental_backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleImageServiceImpl implements VehicleImageService{
    private final Cloudinary cloudinary;
    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository vehicleImageRepository;

    public String uploadVehicleImage(Long vehicleId, MultipartFile file) {
        try {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy xe ID = " + vehicleId));

            // 📤 Upload ảnh lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ev_rental/vehicles", // lưu vào thư mục riêng
                            "resource_type", "image"
                    ));

            String imageUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            // 💾 Lưu thông tin ảnh vào DB
            VehicleImage image = VehicleImage.builder()
                    .vehicle(vehicle)
                    .imageUrl(imageUrl)
                    .publicId(publicId)
                    .description("Ảnh xe tải lên Cloudinary")
                    .build();

            vehicleImageRepository.save(image);

            return imageUrl;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload lên Cloudinary: " + e.getMessage());
        }
    }
}
