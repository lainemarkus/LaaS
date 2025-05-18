#!/bin/bash
echo "Starting..."

sudo yum install -y docker

sudo service docker start


# Login to Docker
source ./access.sh
sudo docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"
sudo docker pull $DOCKER_USERNAME/customer:1.0.0-SNAPSHOT
sudo docker run -d --name crosssellingrecommendation -p 8080:8080 $DOCKER_USERNAME/customer:1.0.0-SNAPSHOT
