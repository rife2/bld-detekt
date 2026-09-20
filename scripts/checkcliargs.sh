#!/bin/bash

main=dev.detekt.cli.Main
new=/tmp/checkcliargs-new
old=/tmp/checkcliargs-old

java -cp "examples/lib/bld/.sandbox/bld-detekt/*" $main --help >$new
java -cp "lib/provided/*" $main --help >$old

diff $old $new

rm -rf $new $old
