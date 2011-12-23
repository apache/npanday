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
package npanday.model.compiler.plugins.io;

import npanday.model.compiler.plugins.io.xpp3.CompilerPluginXpp3Reader

class CompilerPluginXpp3ReaderTest
{
  @org.junit.Test
  void passes()
  {
    def xpp3Reader = new CompilerPluginXpp3Reader();
    def stream = getClass().getResourceAsStream("/sample-compiler-plugins.xml")
    assert stream != null : "couldn't find sample xml"
    def xmlStream = new InputStreamReader(stream)
    def model = xpp3Reader.read(xmlStream)

    assert model != null
    assert model.compilerPlugins != null
    assert model.compilerPlugins.size() == 1
    assert model.compilerPlugins[0].vendorVersion == "1"
  }
}
