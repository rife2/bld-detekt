#!/bin/bash

MAIN="io.gitlab.arturbosch.detekt.cli.Main"
TMPNEW=/tmp/checkcliargs-new
TMPOLD=/tmp/checkcliargs-old

java -cp "lib/compile/*:examples/lib/bld/*" $MAIN --help >$TMPNEW
java -cp "examples/lib/bld/*" $MAIN --help >$TMPOLD

diff $TMPOLD $TMPNEW

rm -rf $TMPNEW $TMPOLD
