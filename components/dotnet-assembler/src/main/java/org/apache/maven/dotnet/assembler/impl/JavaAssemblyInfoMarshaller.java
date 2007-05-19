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
package org.apache.maven.dotnet.assembler.impl;

import org.apache.maven.dotnet.assembler.AssemblyInfoMarshaller;
import org.apache.maven.dotnet.assembler.AssemblyInfo;
import org.apache.maven.dotnet.model.assembly.plugins.AssemblyPlugin;

import java.io.OutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.maven.project.MavenProject;

/**
 * Provides services for writing out the AssemblyInfo entries for Java, where each entry begins with '/**@'  and ends with '*\/'
 *
 * @author Shane Isbell
 */
final class JavaAssemblyInfoMarshaller
    implements AssemblyInfoMarshaller
{

    /**
     * The assembly plugin model that contains information used in writing of the AssemblyInfo class.
     */
    private AssemblyPlugin plugin;

    /**
     * @see AssemblyInfoMarshaller#marshal(org.apache.maven.dotnet.assembler.AssemblyInfo, org.apache.maven.project.MavenProject,
     *      java.io.OutputStream)
     */
    public void marshal( AssemblyInfo assemblyInfo, MavenProject mavenProject, OutputStream outputStream )
        throws IOException
    {
        String src = mavenProject.getBasedir() + "/target/build-sources";
        StringBuffer sb = new StringBuffer();
        sb.append( "import System.Reflection;\r\n" )
            .append( "import System.Runtime.CompilerServices.*;r\n" )
            .append( createEntry( "Description", assemblyInfo.getDescription() ) )
            .append( createEntry( "Title", assemblyInfo.getTitle() ) )
            .append( createEntry( "Company", assemblyInfo.getCompany() ) )
            .append( createEntry( "Product", assemblyInfo.getProduct() ) )
            .append( createEntry( "Copyright", assemblyInfo.getCopyright().replace( "\"", "\\" ) ) )
            .append( createEntry( "Trademark", assemblyInfo.getTrademark() ) )
            .append( createEntry( "Culture", assemblyInfo.getCulture() ) )
            .append( createEntry( "Version", assemblyInfo.getVersion() ) )
            .append( createEntry( "Configuration", assemblyInfo.getConfiguration() ) );
        FileOutputStream man = null;
        try
        {
            String groupIdAsDir = mavenProject.getGroupId().replace( ".", File.separator );
            File file = new File( src + "/META-INF/" + groupIdAsDir );
            file.mkdirs();
            man = new FileOutputStream(
                src + "/META-INF/" + groupIdAsDir + File.separator + "AssemblyInfo." + plugin.getExtension() );
            man.write( sb.toString().getBytes() );
        }
        catch ( IOException e )
        {
            throw new IOException();
        }
        finally
        {
            if ( man != null )
            {
                man.close();
            }
        }
    }
    
    /**
     * @see AssemblyInfoMarshaller#unmarshall(java.io.InputStream)
     */
    public AssemblyInfo unmarshall( InputStream inputStream )
        throws IOException
    {
        throw new IOException("This method is not implemented");
    }

    /**
     * @see AssemblyInfoMarshaller#init(org.apache.maven.dotnet.model.assembly.plugins.AssemblyPlugin)
     */
    public void init( AssemblyPlugin plugin )
    {
        this.plugin = plugin;
    }

    /**
     * Returns an assembly entry with a name-value pair beginning with '/**@'  and ending with '*\/'
     *
     * @param name  the name of the assembly entry
     * @param value the value of the assembly entry
     * @return an assembly entry with a name-value pair beginning with '/**@'  and ending with '*\/'
     */
    private String createEntry( String name, String value )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "/**@assembly: Assembly" ).append( name ).append( "(\"" ).append( value ).append( "\")*/" ).append(
            "\r\n" );
        return sb.toString();
    }
}
