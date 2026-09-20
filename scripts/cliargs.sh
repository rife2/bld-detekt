#!/bin/bash

java -cp "lib/provided/*" dev.detekt.cli.Main --help |\
grep "^    --.*" |\
sed -e "s/    //" -e "s/, .*//" -e '/version/d' -e '/help/d' |\
sort |\
sed -e '$s/,//' > "src/test/resources/detekt-args.txt"

