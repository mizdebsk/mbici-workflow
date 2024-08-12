#!/bin/bash
#-
# Copyright (c) 2021 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Author: Mikolaj Izdebski

set -e

native_image=false
if [ "$1" = "--native" ]; then
    native_image=true
fi

rm -rf target/
javac -d target $(find src -name \*.java)
jar -cfe target/mbici-wf.jar org.fedoraproject.mbi.ci.Main -C target org

if $native_image; then
    $JAVA_HOME/bin/native-image -jar target/mbici-wf.jar -o target/mbici-wf
else
    (echo '#!/usr/bin/java -jar' && cat target/mbici-wf.jar) >target/mbici-wf
    chmod +x target/mbici-wf
fi
