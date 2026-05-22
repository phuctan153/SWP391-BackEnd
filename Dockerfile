# 1. Giai đoạn Build: Tải Maven và đóng gói code
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build ra file .jar và bỏ qua bước chạy test
RUN mvn clean package -DskipTests

# 2. Giai đoạn Run: Chạy ứng dụng với JRE nhẹ gọn
FROM eclipse-temurin:21-jre
WORKDIR /app
# Lấy file .jar từ giai đoạn trước mang sang
COPY --from=build /app/target/*.jar app.jar
# Mở cổng động cho Render
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]