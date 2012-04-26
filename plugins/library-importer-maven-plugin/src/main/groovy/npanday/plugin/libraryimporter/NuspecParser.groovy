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



package npanday.plugin.libraryimporter

import npanday.nuget.NugetVersionSpec
import com.google.common.base.Strings;

/**
 * Very simple nuspec parser
 */
public class NuspecParser
{
    public static NuspecMetadata parse( File packageNuspec )
    {
        def xml = new XmlSlurper().parse(packageNuspec)

        NuspecMetadata meta = new NuspecMetadata();

        meta.id = xml.metadata.id;
        meta.title = xml.metadata.title;
        meta.licenseUrl = xml.metadata.licenseUrl;
        meta.projectUrl = xml.metadata.projectUrl;
        meta.authors = xml.metadata.authors;
        meta.title = xml.metadata.title;
        meta.summary = xml.metadata.summary;
        meta.copyright = xml.metadata.copyright;
        meta.description = xml.metadata.description;

        meta.dependencies = (List<NuspecDependency>)new ArrayList<NuspecDependency>()

        xml.metadata.dependencies.dependency.each{
            NuspecDependency dep = new NuspecDependency(id: it.@id.text())

            if (!Strings.isNullOrEmpty(it.@version.text())){
                dep.version = NugetVersionSpec.parse(it.@version.text())
            }

            meta.dependencies.add(dep);
        }

        return meta;
    }
}

