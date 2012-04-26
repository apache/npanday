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

import com.google.common.collect.Lists
import org.apache.maven.plugin.MojoExecutionException

/**
 * Very simple nuspec parser
 */
public class ManifestInfoParser
{
    public static AssemblyInfo parse( File manifestInfo, String assemblyName )
    {
        def xml = new XmlSlurper().parse( manifestInfo )
        def found = null;
        xml.assembly.each {
            if ( it.@name.text().equals( assemblyName ) )
            {
                def asm = new AssemblyInfo()

                copyAttribs( it, asm )

                asm.references = Lists.newArrayList()
                it.references.assembly.each {
                    def refAsm = new AssemblyInfo()
                    copyAttribs( it, refAsm )
                    asm.references.add( refAsm )
                }

                found = asm
            }
        }

        if ( found != null ) return found;

        throw new MojoExecutionException( "NPANDAY-143-000: Could not find infors for $assemblyName in $manifestInfo" )
    }

    static void copyAttribs( groovy.util.slurpersupport.NodeChild xml, AssemblyInfo assemblyInfo )
    {
        assemblyInfo.name = xml.@name.text()
        assemblyInfo.culture = xml.@culture.text()
        assemblyInfo.publicKeyToken = xml.@publicKeyToken.text()
        assemblyInfo.strongName = xml.@strongName.text()
        assemblyInfo.version = xml.@version.text()
    }
}

