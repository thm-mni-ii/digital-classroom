@echo off

git checkout master && git pull
git tag -a %~1 -m "Version %~1"
git push origin --tags

echo -e "\033[1;34mVersion %~1 Relased\033[0m"