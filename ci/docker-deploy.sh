#!/bin/bash

branch=$1
tag=$2

function dockerPush(){
    tag=$1
    echo "tag is: "$tag

    docker tag feedbacksystem_http thmmniii/digital-classroom:$tag

    docker push thmmniii/digital-classroom:$tag
}

echo "START DOCKER DEPLOY"

echo $DOCKER_PWD | docker login -u $DOCKER_LOGIN --password-stdin

docker-compose build

echo "DOCKER IMAGES"
docker images

if [[ -z "$tag" || "master" == "$branch" ]]
    then
      dockerPush dev-latest
    else
      dockerPush $tag
    fi
