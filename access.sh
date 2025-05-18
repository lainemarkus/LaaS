#!/bin/bash

# JAVA path
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-jdk-17/Contents/Home
export PATH=/Library/Java/JavaVirtualMachines/graalvm-jdk-17/Contents/Home/bin:"$PATH"

# aws access variables (replace with your own)
aws_access_key_id="YourAWSAccessKeyId"
aws_secret_access_key="YourAWSSecretAccessKey"
aws_session_token="YourAWSSessionToken"

# docker access variables (replace with your own)  
yourDockerUsername="yourDockerUsername"  
yourDockerPassword="yourDockerPassword"

# exporting all variables to be used by next scripts
export AWS_ACCESS_KEY_ID=$aws_access_key_id
export AWS_SECRET_ACCESS_KEY=$aws_secret_access_key
export AWS_SESSION_TOKEN=$aws_session_token
export DOCKER_USERNAME=$yourDockerUsername
export DOCKER_PASSWORD=$yourDockerPassword


