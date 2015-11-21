#!/bin/bash
#
# Copyright © 2015 Cask Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

__tmpdir=/tmp/cdap-examples.$$
mkdir -p ${__tmpdir}
cp -a /opt/cdap/sdk/examples ${__tmpdir}
if [ -f /tmp/mavenrepo.tar.bz2 ]; then
  mkdir -p ~cdap/.m2
  cd ~cdap/.m2
  tar xjf /tmp/mavenrepo.tar.bz2
  mv mavenrepo repository
fi

chown -R cdap ${__tmpdir} ~cdap
su - cdap -c "cd ${__tmpdir}/examples && MAVEN_OPTS='-Xmx3072m -XX:MaxPermSize=256m' mvn package -DskipTests" || exit 1
rm -rf ${__tmpdir}

exit 0
