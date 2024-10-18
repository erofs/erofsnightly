#!/bin/sh
# This shell script must be run as root

run_in_chroot()
{
    echo "Running in chroot: $1"
    # Note: we execute the command in a login shell rather than execute it
    # directly because this makes the $PATH get set up correctly.
    DEBIAN_FRONTEND=noninteractive DEBCONF_NONINTERACTIVE_SEEN=true \
	LC_ALL=C LANGUAGE=C LANG=C chroot "$ROOTDIR" /bin/sh -l -c "$1"
}

MIRROR=http://mirrors.kernel.org/debian
ROOTDIR=rootfs
PACKAGES="make gcc flex bison libelf-dev libssl-dev dwarves"
if test -f kbuild-packages ; then
    PACKAGES="$PACKAGES $(cat kbuild-packages)"
fi
PACKAGES=$(echo $PACKAGES | sed 's/ /,/g')

apt-get install -y debootstrap
mkdir -p $ROOTDIR
debootstrap --variant=minbase --include=$PACKAGES buster $ROOTDIR $MIRROR
tar -C basic-overlay \
	--owner=root --group=root --mode=go+u-w -c . | tar -C $ROOTDIR -x
run_in_chroot "systemctl disable systemd-timesyncd"
run_in_chroot "systemctl enable kbuild.service"

# clean up useless files
find $ROOTDIR/usr/share/doc -type f | grep -v copyright | xargs rm
find $ROOTDIR/usr/share/doc -mindepth 2 -type l | xargs rm
find $ROOTDIR/usr/share/doc -type d | xargs rmdir --ignore-fail-on-non-empty -p
rm -rf $ROOTDIR/usr/share/man
find $ROOTDIR/var/log -type f | xargs rm

bin/mkfs.erofs -zlz4hc,12 -C32768 --random-pclusterblks $1 $ROOTDIR
bin/fsck.erofs $1 || exit 1
