#!/bin/bash

# Exit on failure
set -e

case `uname` in
    Linux)
        export CC=gcc
        export CXX=g++
        export LD=g++

        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"
        LDFLAGS="-lpthread -ltermcap"

        # todo nice to have
        #LDFLAGS="$LDFLAGS -Wl,-z,defs"
    ;;
    Darwin)
        CC=clang
        CXX=clang++
        LD=clang++

        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/darwin/"
        LDFLAGS="-ltermcap -L/usr/local/opt/libffi/lib"
    ;;
    *)
        echo Unknown environment: `uname`
        exit 1
    ;;
esac

LLVM_CONFIG=/llvm/build/bin/llvm-config

echo `$LLVM_CONFIG --version`

LLVM_LIBS="irreader transformutils"
LIBS=`$LLVM_CONFIG --libs $LLVM_LIBS`

CFLAGS=`$LLVM_CONFIG --cflags`
CXXFLAGS=`$LLVM_CONFIG --cxxflags`

CPPFLAGS=`$LLVM_CONFIG --cppflags`
LDFLAGS="`$LLVM_CONFIG --ldflags` -v $LDFLAGS"

LLVM_INCLUDE="-I`$LLVM_CONFIG --includedir`"

DEBUG="-g -coverage"
COMMONFLAGS="-fPIC $DEBUG"

SRC_DIR="src/main/cpp"

JAVA_OUT="target/generated-sources/java/ru/urururu/sanity/cpp/llvm"
mkdir -p $JAVA_OUT

CPP_OUT="target/generated-sources/jni"
mkdir -p $CPP_OUT

OBJ_DIR="target/native/static"
mkdir -p $OBJ_DIR

SOBJ_DIR="target/native/shared"
mkdir -p $SOBJ_DIR

swig $CPPFLAGS -java -outdir $JAVA_OUT -package ru.urururu.sanity.cpp.llvm -o $CPP_OUT/bitreader_wrap.c -v $SRC_DIR/bitreader.i

$CC -c $CPP_OUT/bitreader_wrap.c $JAVA_INCLUDES $CFLAGS $COMMONFLAGS -o $OBJ_DIR/wrappers.o

$CXX -c $SRC_DIR/helpers.cpp $CXXFLAGS $COMMONFLAGS -o $OBJ_DIR/helpers.o

$CXX -shared -o $SOBJ_DIR/$DLL_NAME $OBJ_DIR/wrappers.o $OBJ_DIR/helpers.o $LIBS $LDFLAGS $DEBUG
