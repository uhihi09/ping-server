# Render에 최적화된 멀티 스테이지 빌드

# Stage 1: Build
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /build

# Gradle 래퍼 및 설정 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src src

# 테스트 없이 빌드 (빠른 배포)
RUN ./gradlew bootJar --no-daemon -x test

# JAR 파일 확인
RUN ls -la build/libs/

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# 비루트 사용자 생성 (보안)
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 소유권 변경
RUN chown -R spring:spring /app

USER spring:spring

# Render는 PORT 환경변수 사용
EXPOSE ${PORT:-8080}

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/api/actuator/health || exit 1

# JVM 최적화 옵션 포함
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dserver.port=${PORT}", \
  "-Dspring.profiles.active=prod", \
  "-jar", \
  "app.jar"]