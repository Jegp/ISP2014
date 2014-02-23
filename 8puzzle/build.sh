#!/bin/zsh

##
## Compiles, linkes and builds dogfight with 
## emscripten/ASM
##
EMSCRIPTEN="/home/jens/workspace/asm/emscripten"

rm -rf CMakeFiles/ CMakeCache.txt
$EMSCRIPTEN/emconfigure cmake .
$EMSCRIPTEN/emmake make VERBOSE=1 -j 4
mv eight eight.bc
$EMSCRIPTEN/emcc eight.bc -O2 -s ASM_JS=1 -o index.html
