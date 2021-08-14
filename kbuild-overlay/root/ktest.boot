#!/bin/bash -e

printf "Building kernel in QEMU..."

#mkdir -p /tmp/bin
#export PATH="$PATH:/tmp/bin"
#wget https://raw.githubusercontent.com/intel/lkp-tests/master/sbin/make.cross \
#	--no-check-certificate -O /tmp/bin/make.cross
#chmod +x /tmp/bin/make.cross
#export COMPILER_INSTALL_PATH="/tmp/0day"

# compile kernel source
mkdir -p /tmp/lowerdir /tmp/kbuild &&
mount -t erofs -oro /dev/sda /tmp/lowerdir &&
mount /dev/sdb /mnt &&
mkdir -p /mnt/upperdir /mnt/workdir
mount -t overlay -o lowerdir=/tmp/lowerdir,upperdir=/mnt/upperdir,workdir=/mnt/workdir \
       overlay /tmp/kbuild &&
cd /tmp/kbuild &&
make defconfig ARCH=x86_64 &&
make ARCH=x86_64 -j6 &&
make mrproper && cd / &&
umount /tmp/kbuild &&
umount /tmp/lowerdir &&
echo 0 > /mnt/exitstatus && sync &&
umount /mnt
