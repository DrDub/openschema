#!/bin/bash

SCHEMATOP=/home/pablo/research/planner/openschema
JAVA_HOME=/usr/java1.4

for i in $SCHEMATOP/external/*jar
do
  CLASSPATH=$CLASSPATH:$i
done

CLASSPATH=$CLASSPATH:$SCHEMATOP/build:$SCHEMATOP/samples
$JAVA_HOME/bin/java -cp $CLASSPATH  TemplateSystem "$@"

