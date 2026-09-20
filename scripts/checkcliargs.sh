#!/bin/bash

main=dev.detekt.cli.Main
new=/tmp/checkcliargs-new
old=/tmp/checkcliargs-old

java -cp "lib/compile/examples/lib/bld/.sandbox/bld-dokka/*" $main --help >$new
java -cp "examples/lib/provided/*" $main --help >$old

diff $old $new

rm -rf $new $old
