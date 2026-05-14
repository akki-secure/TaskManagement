# マルチステージビルド
# ステージ1: ビルド（Gradleでjarファイルを作る）
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradleのキャッシュを活かすため、先にビルドファイルだけコピー
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# ソースコードをコピーしてビルド
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ステージ2: 実行（JREのみ。サイズを小さくする）
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# セキュリティ: rootではなく専用ユーザーで実行
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
