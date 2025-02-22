#!/bin/sh
set -eu
echo "${PLAN}" | base64 -d | zcat >plan.xml
echo "${PLATFORM}" | base64 -d | zcat >platform.xml
echo "${SUBJECT}" | base64 -d | zcat >subject.xml

echo === CPU info ===
lscpu
echo
echo === Memory info ===
free -h
echo

set -x

mbi generate \
	 -plan plan.xml \
	 -platform platform.xml \
	 -subject subject.xml \
	 -workflow workflow.xml

mbi run \
	 -batch \
	 -maxCheckoutTasks 10 \
	 -maxSrpmTasks 1 \
	 -maxRpmTasks 1 \
	 -workflow workflow.xml \
	 -resultDir result \
	 -cacheDir cache \
	 -workDir work

mbi report \
	 -full \
	 -plan plan.xml \
	 -platform platform.xml \
	 -subject subject.xml \
	 -workflow workflow.xml \
	 -resultDir result \
	 -reportDir "${TMT_TEST_DATA}"
