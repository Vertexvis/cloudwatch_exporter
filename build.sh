#!/bin/sh
. ./scripts/service.sh
. ./scripts/utils.sh

if [ "$SKIP_DOCKER" != "true" ]; then
    LOG "Starting Docker build, set SKIP_DOCKER=true to skip..."
    docker build -t ${SERVICE_NAME}:${GIT_COMMIT_SHORT} -f ./Dockerfile --pull .

    ERR $? "Docker build failed"
fi


if [ "$SKIP_PUBLISH" != "true" ]; then
    LOG "Starting Docker publish, set SKIP_PUBLISH=true to skip..."
    if [ "$BRANCH_NAME" = "" ]; then
        LOG "Not publishing locally, use ${SERVICE_NAME}:${LOCAL_TAG}"
	docker tag ${SERVICE_NAME}:${GIT_COMMIT_SHORT} ${SERVICE_NAME}:${LOCAL_TAG}
    else
        . ./scripts/version.sh

        tagAndPush ${VERSION}
        tagAndPush ${GIT_COMMIT_SHORT}
        if [ "$BRANCH_NAME" = "master" ]; then
            tagAndPush latest
            tagAndPush master-${GIT_COMMIT_SHORT}
        else
            tagAndPush branch-${BRANCH_NAME}
        fi
    fi 
fi

