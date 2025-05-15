#!/bin/bash
echo "Starting..."

sudo yum install -y docker

sudo service docker start


sudo docker login -u "lainemarkus" -p "K]jTp,6f4,4BMLM"
sudo docker pull lainemarkus/discountcoupon:1.0.0-SNAPSHOT
sudo docker run -d --name discountcoupon -p 8080:8080 lainemarkus/discountcoupon:1.0.0-SNAPSHOT