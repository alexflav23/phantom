#!/usr/bin/env bash
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "develop" ];
then

    echo "The current JDK version is ${TRAVIS_JDK_VERSION}"
    echo "The current Scala version is ${TRAVIS_SCALA_VERSION}"

    CURRENT_VERSION = "$(sbt version)"
    echo "Bumping release version with a patch increment from ${CURRENT_VERSION}"
    sbt version-bump-patch

    NEW_VERSION = "$(sbt version)"
    echo "Creating Git tag for version ${NEW_VERSION}"

    echo "Pushing tag to GitHub."
    git push --tags --quiet "https://${github_token}@${GH_REF}" master:gh-pages > /dev/null 2>&1

    echo "Publishing signed artifact"
    sbt bintray:publish

    git checkout master
    git merge develop

    git push --all --quiet "https://${github_token}@${GH_REF}" master:gh-pages > /dev/null 2>&1


else
    echo "This is either a pull request or the branch is not develop, deployment not necessary"
fi
