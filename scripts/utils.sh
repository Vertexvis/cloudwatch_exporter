#!/bin/sh
export LOCAL_TAG="development"
export AWS_ECR="${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com"
export DOCKER_BASE="${AWS_ECR}/${SERVICE_NAME}"
export GIT_COMMIT_SHORT=`git rev-parse --short HEAD`

ERR() {
    if [ "$1" != "0" ]; then
        echo "$2"
        exit 1
    fi
}

ERRECHO() {
    >&2 echo "$@";
}

LOG() {
    echo "$1"
}

tagAndPush() {
    LOG "Publishing to ${DOCKER_BASE}:$1"
    docker tag ${SERVICE_NAME}:${GIT_COMMIT_SHORT} ${DOCKER_BASE}:$1
    docker push ${DOCKER_BASE}:$1
    ERR $? "Docker publish failed, some artifacts may not have published"
}

handleSignal() {
    ERRECHO "Signal $@ received"
    consulDeregister
    exit 0
}
