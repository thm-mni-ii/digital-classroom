#!/bin/bash

function exitOnError() {
    res=$?
    if [ $res -ne 0 ]; then
        echo -e "\033[0;31;1mCould not create Relase\033[0m"
        exit $res
    fi
}

git checkout master && git pull
git tag -a $1 -m "Version ${1}"
exitOnError

git push origin --tags
exitOnError

echo -e "\033[1;34mVersion ${1} Relased\033[0m"