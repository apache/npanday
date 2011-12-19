#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

#  Workaround for http://jira.codehaus.org/browse/MNG-1911
#  The first time you build a new NPanday version, the compile-plugin has
#  to be installed into the repository already.

set -e

echo ###################################################################
echo  Bootstrapping the NPanday Compile Plugin
echo ###################################################################

mvn clean install -Dbootstrap --projects org.apache.npanday.plugins:maven-compile-plugin --also-make

echo ###################################################################
echo  SUCCESS! Now you should be able to build using 'mvn install'
echo ###################################################################

