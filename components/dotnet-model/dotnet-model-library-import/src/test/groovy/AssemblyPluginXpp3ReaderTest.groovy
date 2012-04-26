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





import npanday.model.library.imports.io.xpp3.LibraryImportsXpp3Reader

class AssemblyPluginXpp3ReaderTest
{
  @org.junit.Test
  void passes()
  {
    def xpp3Reader = new LibraryImportsXpp3Reader();
    def stream = getClass().getResourceAsStream("/sample-nuget-imports.xml")
    assert stream != null : "couldn't find sample xml"
    def xmlStream = new InputStreamReader(stream)
    def model = xpp3Reader.read(xmlStream)

    assert model != null
    assert model.nugetImports != null
    assert model.nugetImports.size() == 1
    assert model.nugetImports[0] != null
    assert model.nugetImports[0].packageName == "NUnit"
    assert model.nugetImports[0].versions != null
    assert model.nugetImports[0].versions.size() == 2
    assert model.nugetImports[0].versions[0].source != null
    assert model.nugetImports[0].versions[0].maven != null

    assert model.nugetImports[0].libraryDirectories.defaultDirectory == "net40"

    assert model.nugetImports[0].referenceMappings.size() == 1
    assert model.nugetImports[0].referenceMappings[0].name == "AssemblyName"
    assert model.nugetImports[0].referenceMappings[0].ignore

    assert model.nugetSources != null
    assert model.nugetSources.addNugetGallery
    assert model.nugetSources.customSources.size() == 1
    assert model.nugetSources.customSources[0] == "package-source"

  }
}
