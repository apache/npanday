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



def projectDir = new File( basedir, "target\\generated-projects\\FluentAssertions_FluentAssertions_dotnet-library_1.7.1" )
assert projectDir.exists()

assert new File( projectDir, "pom.xml" ).exists()
assert new File( projectDir, "FluentAssertions.dll" ).exists()

def project = new XmlSlurper().parse(new File(projectDir, "pom.xml"))

assert project.description == "A very extensive set of extension methods for .NET 3.5, 4.0 and Silverlight 4.0 that allow you to more naturally specify the expected outcome of a TDD or BDD-style unit test."
assert project.url == "http://fluentassertions.codeplex.com"
assert project.developers.developer[0].name == "Dennis Doomen"
assert project.developers.developer[1].name == "Martin Opdam"

return true;
