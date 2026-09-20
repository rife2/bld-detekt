#!/bin/bash

java -cp "examples/lib/bld/.sandbox/bld-detekt/*" dev.detekt.cli.Main --help |\
grep "^    --.*" |\
sed -e "s/    //" -e "s/, .*//" -e '/version/d' -e '/help/d' |\
sort |\
sed -e '$s/,//' > "src/test/resources/detekt-args.txt"

