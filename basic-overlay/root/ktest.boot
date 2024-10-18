#!/bin/bash -e

printf "Basic tests run in QEMU ..."
# Download dependent test tools
cd /dev
wget --no-check-certificate https://github.com/lz4/lz4/archive/refs/tags/v1.9.4.tar.gz -O lz4-1.9.4.tar.gz 
tar -zxvf lz4-1.9.4.tar.gz
make BUILD_SHARED=no -C lz4-1.9.4 && lz4libdir=$(pwd)/lz4-1.9.4/lib
git clone git://git.kernel.org/pub/scm/linux/kernel/git/xiang/erofs-utils.git -b experimental-tests

cd erofs-utils
./autogen.sh && ./configure \
  --with-lz4-incdir=${lz4libdir} --with-lz4-libdir=${lz4libdir}
make check

# Prepare test data benchmark
cd ../ && mkdir silesia out
wget --no-check-certificate -O silesia.zip https://mattmahoney.net/dc/silesia.zip
unzip silesia.zip -d silesia
./erofs-utils/mkfs/mkfs.erofs -C4096 silesia.erofs.img silesia 
./erofs-utils/fuse/erofsfuse silesia.erofs.img out

# Run stress test
git clone https://github.com/erofs/erofsstress.git
gcc erofsstress/stress.c -o stress
./stress -l100 -p3 ./out
# ./erofs-utils/tests/erofsstress/stress -l100 -p3 ./out
