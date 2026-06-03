REGISTRY := 876622472841.dkr.ecr.ap-east-1.amazonaws.com
IMAGE := ${REGISTRY}/remy/user-service

gen:
	mvn clean generate-sources
gen/jooq:
	mvn clean install -P jooq-codegen
in:
	mvn clean install
mvn/build:
	mvn clean install

run:
	mvn clean install
	java -jar target/user-service-1.0-shaded.jar
up:
	docker run \
	--network="host" \
	--env-file .env \
	$(IMAGE)

# enable builder
buildx:
	docker buildx create --use

build:
	docker buildx build \
	--platform linux/arm64 \
	-t $(IMAGE) \
	--load \
      .

# Optional multi-arch build using buildx (requires container driver or containerd image store)
# Use --push to push to registry; --load supports single-platform only.
build/multi:
	docker buildx build \
	--platform linux/amd64,linux/arm64 \
	-t $(IMAGE) \
	--push \
      .

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