#!/bin/bash

if [[ $1 == "" ]]; then
    echo "arg1 - the path to the projects csv file"
    echo "arg2 - path to pom-modify script"
    echo "arg3 - path to the include.xml file"
    echo "arg4 - path to sub-folder (optional)"
    exit
fi

projfile=${1}

#go through every project
for line in $(cat ${projfile}); do
    
    slug=$(echo ${line} | cut -d',' -f1)
    sha=$(echo ${line} | cut -d',' -f2)
    git clone ${slug}
    cd ${slug##*/}
    git checkout ${sha}

    #build maven in the root
    mvn clean install -fn -DskipTests -Dmaven.javadoc.skip=true
    if [ -z "$4" ]; then
        cd ${4}
    fi
    
    #copy include.xml and run the script that modify the pom.xml file
    cp ${3} .
    bash ${2} .
    
    mvn site
    #now the results should be under target/site/spotbugs.html
    cd ${crnt}
done