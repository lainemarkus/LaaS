#!/bin/bash

source ./access.sh

# === Terraform - RDS ===
cd RDS-Terraform
terraform init
terraform apply -auto-approve
esc=$'\e'
addressDB="$(terraform state show aws_db_instance.example |grep address | sed "s/address//g" | sed "s/=//g" | sed "s/\"//g" |sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g" )"
cd ..

# === Terraform - Camunda ===
cd Camunda-Terraform
terraform init
terraform apply -auto-approve
cd ..

# === Terraform - Kafka ===
cd Kafka
terraform init
terraform apply -auto-approve
esc=$'\e'
addresskafka="$(terraform state show 'aws_instance.exampleKafkaConfiguration[0]'|grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" |sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g" )"
cd ..

# === Deploy Quarkus Microservices ===
for service in Purchase loyaltycard customer shop discountcoupon
  do
    cd microservices/$service/src/main/resources
    sed -i '' "/quarkus.datasource.reactive.url/d" application.properties
    sed -i '' "/quarkus.container-image.group/d" application.properties
    sed -i '' "/kafka.bootstrap.servers/d" application.properties || true
    echo "quarkus.container-image.group=$DockerUsername" >> application.properties
    echo "quarkus.datasource.reactive.url=mysql://$addressDB:3306/quarkus_test_all_operations" >> application.properties
    echo "kafka.bootstrap.servers=$addresskafka:9092" >> application.properties
    cd ../../..

    DockerImage="$(grep -m 1 "<artifactId>" pom.xml|sed "s/<artifactId>//g"|sed "s/<\/artifactId>//g" |sed "s/\"//g"|sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
    DockerImageVersion="$(grep -m 1 "<version>" pom.xml|sed "s/<version>//g"|sed "s/<\/version>//g" |sed "s/\"//g"|sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
    ./mvnw clean package
    cd ../..

    cd Quarkus-Terraform/$service
    sed -i '' "/sudo docker login/d" quarkus.sh
    sed -i '' "/sudo docker pull/d" quarkus.sh
    sed -i '' "/sudo docker run/d" quarkus.sh
    echo "sudo docker login -u \"$DockerUsername\" -p \"$DockerPassword\"" >> quarkus.sh
    echo "sudo docker pull $DockerUsername/$DockerImage:$DockerImageVersion" >> quarkus.sh
    echo "sudo docker run -d --name $DockerImage -p 8080:8080 $DockerUsername/$DockerImage:$DockerImageVersion" >> quarkus.sh
    terraform init
    terraform taint aws_instance.exampleDeployQuarkus
    terraform apply -auto-approve
    cd ../..
  done

# === Terraform - Kong ===
cd KongTerraform
terraform init
terraform apply -auto-approve
cd ..

# === Terraform - Konga ===
cd KongaTerraform
terraform init
terraform apply -auto-approve
cd ..

# === Show PUBLIC DNS entries ===
echo "CAMUNDA IS AVAILABLE HERE:"
cd Camunda-Terraform
addressCamunda="$(terraform state show aws_instance.exampleInstallCamundaEngine |grep public_dns| sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" |sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g" )"
echo "http://$addressCamunda:8080/camunda"
echo
cd ..

echo "KAFKA IS AVAILABLE HERE:"
echo "$addresskafka"
echo

for service in Purchase customer shop loyaltycard discountcoupon
  do
    cd Quarkus-Terraform/$service
    addressMS="$(terraform state show aws_instance.exampleDeployQuarkus |grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" |sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g" )"
    echo "MICROSERVICE $service IS AVAILABLE HERE:"
    echo "http://$addressMS:8080/q/swagger-ui/"
    echo
    cd ../..
  done

echo "RDS IS AVAILABLE HERE:"
cd RDS-Terraform
echo $(terraform state show aws_db_instance.example |grep address)
echo $(terraform state show aws_db_instance.example |grep port)
echo
cd ..

echo "KONG IS AVAILABLE HERE:"
cd KongTerraform
addressKong="$(terraform state show aws_instance.exampleInstallKong |grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" |sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g" )"
echo "http://$addressKong:8000/"
echo
cd ..

echo "KONGA IS AVAILABLE HERE:"
cd KongaTerraform
addressKonga="$(terraform state show aws_instance.exampleInstallKonga |grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" |sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g" )"
echo "http://$addressKonga:1337/"
echo
cd ..