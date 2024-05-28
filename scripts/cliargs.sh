#!/bin/bash

java -cp "lib/compile/*:examples/lib/bld/*" io.gitlab.arturbosch.detekt.cli.Main --help |\
grep "^    --.*" |\
sed -e "s/    //" -e "s/, .*//" -e '/version/d' -e '/help/d' |\
sort |\
sed -e '$s/,//' > "src/test/resources/detekt-args.txt"

