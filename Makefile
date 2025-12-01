REGISTRY := 876622472841.dkr.ecr.ap-east-1.amazonaws.com
IMAGE := ${REGISTRY}/remy/user-service

run:
	mvn clean install
	java -jar target/user-service-1.0.jar
# enable builder
buildx:
	docker buildx create --use

# Optional multi-arch build using buildx (requires container driver or containerd image store)
# Use --push to push to registry; --load supports single-platform only.
build/multi:
	docker buildx build \
	--platform linux/amd64,linux/arm64 \
	--build-arg MAVEN_IMAGE=maven:3.9.6-eclipse-temurin-17 \
	--build-arg JRE_IMAGE=eclipse-temurin:17-jre \
	-t $(IMAGE) \
	--push \
      .

up:
	docker compose up

login:
	aws ecr get-login-password --region ap-east-1 \
  | docker login --username AWS --password-stdin ${REGISTRY}

pull:
	docker pull ${IMAGE}

push:
	docker build -t $(IMAGE) .
	docker push $(IMAGE)

image:
	aws ecr list-images \
  --repository-name remy \
  --region ap-east-1