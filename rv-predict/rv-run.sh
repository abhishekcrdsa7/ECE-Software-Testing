#!/bin/bash

if [[ $1 == "" ]]; then
    echo "arg1 - the path to the projects csv file"
    echo "arg2 - path to pom-modify script"
    echo "arg3 - path to the output file"
    echo "arg4 - path to sub-folder"
    exit
fi

projfile=${1}

for line in $(cat ${projfile}); do
    #https://github.com/project -> tcejorp/moc.buhtig://sptth -> tcejorp/moc.buhtig -> github.com/project
    slug=$(echo ${line} | cut -d',' -f1)
    sha=$(echo ${line} | cut -d',' -f2)
    git clone ${slug}
    cd ${slug##*/}
    git checkout ${sha}
    if [ -z "$4" ]; then
        cd ${4}
    fi
    bash ${2} .
    rm -rf /tmp/rv-predict*
    mvn test
    mv -r /tmp/rv-* ${3}
    rm -rf /tmp/rv-predict*
    cd ${crnt}
done

