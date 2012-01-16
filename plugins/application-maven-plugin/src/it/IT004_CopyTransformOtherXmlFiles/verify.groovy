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

def packageDir = new File(basedir, "target\\packages\\IT004_CopyTransformOtherXmlFiles")
assert packageDir.exists()

// is null.config, because it is neither dll or exe, but pom
// repeating "config.xml" for better assertion error message
assert new File(packageDir, "config.xml").exists()

def xml = new XmlSlurper().parse(new File(packageDir, "config.xml"))
assert xml.connectionStrings[0].add[0].@connectionString == "from-package-transform"

assert new File(packageDir, "config.package.xml").exists() == false
assert new File(packageDir, "config.otherhint.xml").exists() == false
assert new File(packageDir, "excluded.xml").exists() == false
assert new File(packageDir, "pom.xml").exists() == false


return true;
