export DevID =ocid1.compartment.oc1..aaaaaaaadh37bvvavyij7uwekvq32eu6bgb2awddnmucisib6bdbsm4pcieq
gen:
	mvn clean generate-sources
in:
	mvn clean install
run:
	mvn clean install
	java -jar target/auth-service-1.0.jar

# enable builder
buildx:
	docker buildx create --use

build/docker:
	docker build \
	--build-arg MAVEN_IMAGE=maven:3.9.6-eclipse-temurin-21 \
	--build-arg JRE_IMAGE=eclipse-temurin:21-jre \
	-t ap-singapore-1.ocir.io/axfnrpyfvlpv/auth-service:latest .
# Optional multi-arch build using buildx (requires container driver or containerd image store)
# Use --push to push to registry; --load supports single-platform only.
build/multi:
	docker buildx build \
	--platform linux/amd64,linux/arm64 \
	--build-arg MAVEN_IMAGE=maven:3.9.6-eclipse-temurin-17 \
	--build-arg JRE_IMAGE=eclipse-temurin:17-jre \
	-t ap-singapore-1.ocir.io/axfnrpyfvlpv/auth-service:latest \
	--push \
      .
up:
	docker compose up
pull:
	docker pull ap-singapore-1.ocir.io/axfnrpyfvlpv/auth-service:latest
push:
	docker build -t ap-singapore-1.ocir.io/axfnrpyfvlpv/auth-service .
	docker push ap-singapore-1.ocir.io/axfnrpyfvlpv/auth-service
oci/docker/login:
	docker login ap-singapore-1.ocir.io -u axfnrpyfvlpv/hcmut2025sa/anhchanto2025 -p "3)Q3iC3}3vO:V4RMI}tJ"
oci/ns:
	oci os ns get
oci/iam:
	 oci iam user get --user-id $$(oci iam user list --query "data[0].id" --raw-output)
oci/users:
	oci iam user list --all --query "data[].name" --output table
oci/me:
	oci iam user get --user-id $$(oci iam user list --query "data[?name=='ltthang.sdh241'].id | [0]" --raw-output)
oci/ls-domain-in-compartment:
# 	tenancy is also a compartment
	oci iam domain list --compartment-id ocid1.tenancy.oc1..aaaaaaaaxtbkp4ctt6kplr5iuse7e7v2fr6b56wnuydyt3cqz6763imlje6q --all
oci/domain/users:
	oci identity-domains users list --all --query "data.resources[].emails[0].value" --output table  --endpoint https://idcs-c344c7c91bd6488481e5351f77c8b5a8.ap-singapore-idcs-1.identity.ap-singapore-1.oci.oraclecloud.com:443
oci/profile:
	@grep "^\[" ~/.oci/config | tr -d "[]"
oci/image:
	oci artifacts container repository list --compartment-id ocid1.compartment.oc1..aaaaaaaadh37bvvavyij7uwekvq32eu6bgb2awddnmucisib6bdbsm4pcieq
oci/subnet:
	oci network subnet list --compartment-id $(DevID) -vcn-id ocid1.vcn.oc1.ap-singapore-1.amaaaaaa657ylliaykxlcvvqw2mu2z7gigybascjs7yvgjlopdtkcyqp4uwa
oci/pool:
	oci ce node-pool get --node-pool-id ocid1.nodepool.oc1.ap-singapore-1.aaaaaaaadm5rvfwkzh5xozgpxbbte3yfqq4iccnmhreuz6tv2noc47d363fq
oci/instance:
	oci compute instance get --instance-id ocid1.instance.oc1.ap-singapore-1.anzwsljr657yllicgr6p53c3qtjtbwukciksii5pftm27gma57ujfdofczja

stream:
	nats stream ls
stream/add:
	nats stream add auth_stream \
	--subjects auth.* \
	--storage file \
	--replicas 1 \
	--retention limits \
	--discard old \
	--deny-delete \
	--no-deny-purge \
	--allow-rollup \
	--max-age 2h \
	--max-msg-size 200kb \
	--dupe-window 1h \
	--max-msgs 100000 \
	--max-bytes 1Gb \
	--max-msgs-per-subject 1000

stream/del:
	nats stream rm
pub:
	nats pub auth.user_login_subject '{"user_id":1,"email":"thangdeptrai@gmail.com"}'
sub:
	nats sub auth.user_login_subject
consumer:
	nats consumer ls
pub/register:
	nats pub auth.user_register_subject '{"user_id":1,"email":"thangdeptrai@gmail.com"}'
sub/register:
	nats sub auth.user_register_subject
sync:
  rsync -av --progress --exclude='.git' ./auth-service/ ./remy-auth-service/

