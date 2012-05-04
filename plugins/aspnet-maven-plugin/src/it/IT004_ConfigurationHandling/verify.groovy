/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


def packageDir = new File(basedir, "target\\packages\\IT004_ConfigurationHandling")
assert packageDir.exists()

assert new File(packageDir, "web.config").exists()
def xml = new XmlSlurper().parse(new File(packageDir, "web.config"))
assert xml.connectionStrings[0].add[0].@connectionString == "from-package-transform"

assert new File(packageDir, "web.package.config").exists() == false

assert new File(packageDir, "connectionStrings.config").exists()
xml = new XmlSlurper().parse(new File(packageDir, "connectionStrings.config"))
assert xml.connectionStrings[0].add[0].@connectionString == "from-package-transform"

assert new File(packageDir, "connectionStrings.package.config").exists() == false

assert new File(packageDir, "sublevel/web.config").exists()
xml = new XmlSlurper().parse(new File(packageDir, "sublevel/web.config"))
assert xml.connectionStrings[0].add[0].@connectionString == "from-package-transform"

assert new File(packageDir, "sublevel/web.package.config").exists() == false

assert new File(packageDir, "different_extension/config.xml").exists()
xml = new XmlSlurper().parse(new File(packageDir, "different_extension/config.xml"))
assert xml.connectionStrings[0].add[0].@connectionString == "from-package-transform"

assert new File(packageDir, "different_extension/config.package.xml").exists() == false
assert new File(packageDir, "different_extension/config.otherhint.xml").exists() == false
assert new File(packageDir, "different_extension/excluded.xml").exists() == false

assert new File(packageDir, "pom.xml").exists() == false

return true;
