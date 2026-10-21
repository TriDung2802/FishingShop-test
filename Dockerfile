# syntax=docker/dockerfile:1

# ---------- Stage 1: Build WAR bằng Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Tải dependency trước để tận dụng cache layer (chỉ chạy lại khi pom.xml đổi)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

# ---------- Stage 2: Chạy trên Tomcat 11 (Jakarta Servlet 6.1) ----------
FROM tomcat:11.0-jdk21-temurin

# Xóa app mặc định của Tomcat, deploy WAR làm ROOT => truy cập tại http://host:8080/
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/fishingshop.war /usr/local/tomcat/webapps/ROOT.war

# Thư mục lưu ảnh upload (UploadUtil đọc -Dfishing.upload.dir) - nên mount volume vào đây
RUN mkdir -p /data/uploads

# pom.xml biên dịch với --enable-preview nên JVM runtime cũng phải bật cờ này
ENV JAVA_OPTS="--enable-preview -Dfishing.upload.dir=/data/uploads -Dfile.encoding=UTF-8"

# Biến môi trường cho JPAUtil / EmailUtil (truyền khi chạy container, KHÔNG hard-code vào image):
#   DB_HOST (vd: db:5432), DB_NAME, DB_USER, DB_PASSWORD, DB_SSL_MODE (require|disable)
#   GMAIL_EMAIL, GMAIL_APP_PASSWORD

# Chạy bằng user thường thay vì root
RUN useradd -r -u 1001 -g root appuser \
    && chown -R 1001:0 /usr/local/tomcat /data \
    && chmod -R g+rwX /usr/local/tomcat /data
USER 1001

VOLUME ["/data/uploads"]
EXPOSE 8080
CMD ["catalina.sh", "run"]
