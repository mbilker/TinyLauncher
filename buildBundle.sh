#!/bin/sh

cd $(dirname $0)

rm -rf bundle
mkdir bundle
cp -a TinyLauncher.jar bundle
cp -a TinyLauncher.app bundle
cp -a data bundle
echo "Please add your .minecraft folder as data/mc" > bundle/README_FIRST_BEFORE_LAUNCHING
cd bundle
tar jcvf ../TinyLauncherBundle.tar.bz2 .
cd ..
rm -rf bundle
