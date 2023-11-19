FROM  maven:3.9-amazoncorretto-17 as builder
COPY pom.xml .
COPY src ./src
RUN mvn package

FROM eclipse-temurin:17-jdk-jammy
RUN apt-get update && apt-get install -y \
    software-properties-common
RUN add-apt-repository universe
RUN apt-get update && apt-get install -y \
    python3.4 \
    python3-pip
ENV AWS_ACCESS_KEY_ID=""
ENV AWS_SECRET_ACCESS_KEY=""
ENV AWS_REGION="eu-west-1"
ENV BUCKET_NAME="kandidat-id-2012"
RUN pip install awscli
COPY --from=builder /target/*.jar /app/application.jar
ENTRYPOINT ["java","-jar","/app/application.jar"]