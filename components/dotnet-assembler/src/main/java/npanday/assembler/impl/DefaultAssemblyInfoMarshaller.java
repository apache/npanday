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
package npanday.assembler.impl;

import npanday.assembler.AssemblyInfoMarshaller;
import npanday.assembler.AssemblyInfo;
import npanday.model.assembly.plugins.AssemblyPlugin;

import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Provides services for writing out the AssemblyInfo entries using the bracket convention [assembly:
 *
 * @author Shane Isbell
 */
final class DefaultAssemblyInfoMarshaller
    implements AssemblyInfoMarshaller
{

    /**
     * The assembly plugin model that contains information used in writing of the AssemblyInfo class.
     */
    private AssemblyPlugin plugin;

    /**
     * @see AssemblyInfoMarshaller#marshal(npanday.assembler.AssemblyInfo, org.apache.maven.project.MavenProject,
     *      java.io.OutputStream)
     */
    public void marshal( AssemblyInfo assemblyInfo, MavenProject mavenProject, OutputStream outputStream )
        throws IOException
    {
        String src = mavenProject.getBuild().getDirectory() + "/build-sources";
        StringBuffer sb = new StringBuffer();
        sb.append( "using System.Reflection;\r\n" )
            .append( "using System.Runtime.CompilerServices;\r\n" )
            .append( createEntry( "Description", assemblyInfo.getDescription() ) )
            .append( createEntry( "Title", assemblyInfo.getTitle() ) )
            .append( createEntry( "Company", assemblyInfo.getCompany() ) )
            .append( createEntry( "Product", assemblyInfo.getProduct() ) )
            .append( createEntry( "Copyright", assemblyInfo.getCopyright().replace( "\"", "\\" ) ) )
            .append( createEntry( "Trademark", assemblyInfo.getTrademark() ) )
            .append( createEntry( "Culture", assemblyInfo.getCulture() ) )
            .append( createEntry( "Version", assemblyInfo.getVersion() ) )
            .append( createEntry( "InformationalVersion", assemblyInfo.getInformationalVersion() ) )
            .append( createEntry( "Configuration", assemblyInfo.getConfiguration() ) );
        if ( assemblyInfo.getKeyName() != null )
        {
            sb.append( createEntry( "KeyName", assemblyInfo.getKeyName() ) );
        }
        if ( assemblyInfo.getKeyFile() != null )
        {
            sb.append( createEntry( "KeyFile", assemblyInfo.getKeyFile().getAbsolutePath().replace( "\\", "\\\\" ) ) );
        }

        boolean wroteCustomStringAttribute = false;
        for(Entry<String, String> e: assemblyInfo.getCustomStringAttributes().entrySet()) {
            if(StringUtils.isEmpty(e.getValue()))
                continue;
            sb.append(createCustomStringEntry(e.getKey(), e.getValue()));
            wroteCustomStringAttribute = true;
        }
        
        if(wroteCustomStringAttribute) {
            final String customClass = "\n" + //
                "[System.AttributeUsage(System.AttributeTargets.Assembly, AllowMultiple = true)]\n" + //
                "class CustomStringAttribute : System.Attribute {\n" + //
                "  public CustomStringAttribute(string name, string value) {\n" + //
                "  }\n" + // 
                "}\n"; //
            sb.append(customClass);
        }
        
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
            throw new IOException( "NPANDAY-022-000: Failed to generate AssemblyInfo", e );
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
     * @see AssemblyInfoMarshaller#init(npanday.model.assembly.plugins.AssemblyPlugin)
     */
    public void init( AssemblyPlugin plugin )
    {
        this.plugin = plugin;
    }

    /**
     * @see AssemblyInfoMarshaller#unmarshall(java.io.InputStream)
     */
    public AssemblyInfo unmarshall( InputStream inputStream )
        throws IOException
    {
        AssemblyInfo assemblyInfo = new AssemblyInfo();
        BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );
        String line;
        while ( ( line = reader.readLine() ) != null )
        {
            if ( !line.trim().startsWith("//") )
			{
				String[] tokens = line.split( "[:]" );

				if ( tokens.length == 2 )
				{
					String[] assemblyTokens = tokens[1].split( "[(]" );
					String name = assemblyTokens[0].trim();
					String value = assemblyTokens[1].trim().split( "[\"]" )[1].trim();
					setAssemblyInfo( assemblyInfo, name, value );
				}
            }
        }
        return assemblyInfo;
    }

    /**
     * Sets the specified value within the specified assembly info
     *
     * @param assemblyInfo the assembly info to set information on
     * @param name the name of the assembly info field: AssemblyTitle, AssemblyDescription, ...
     * @param value the value associated with the specified name
     * @throws IOException if the assembly info is invalid
     */
    private void setAssemblyInfo( AssemblyInfo assemblyInfo, String name, String value )
        throws IOException
    {
        if ( !name.startsWith( "Assembly" ) )
        {
            throw new IOException(
                "NPANDAY-022-001: Invalid assembly info parameter: Name = " + name + ", Value = " + value );
        }
        if ( name.equals( "AssemblyDescription" ) )
        {
            assemblyInfo.setDescription( value );
        }
        else if ( name.equals( "AssemblyInformationalVersion" ) )
        {
            assemblyInfo.setInformationalVersion( value );
        }
        else if ( name.equals( "AssemblyTitle" ) )
        {
            assemblyInfo.setTitle( value );
        }
        else if ( name.equals( "AssemblyCompany" ) )
        {
            assemblyInfo.setCompany( value );
        }
        else if ( name.equals( "AssemblyProduct" ) )
        {
            assemblyInfo.setProduct( value );
        }
        else if ( name.equals( "AssemblyCopyright" ) )
        {
            assemblyInfo.setCopyright( value );
        }
        else if ( name.equals( "AssemblyTrademark" ) )
        {
            assemblyInfo.setTrademark( value );
        }
        else if ( name.equals( "AssemblyCulture" ) )
        {
            assemblyInfo.setCulture( value );
        }
        else if ( name.equals( "AssemblyVersion" ) )
        {
            assemblyInfo.setVersion( value );
        }
        else if ( name.equals( "AssemblyConfiguration" ) )
        {
            assemblyInfo.setConfiguration( value );
        }
        else if ( name.equals( "AssemblyKeyFile" ) )
        {
            assemblyInfo.setConfiguration( value );
        }
        else if ( name.equals( "AssemblyKeyName" ) )
        {
            assemblyInfo.setConfiguration( value );
        }
    }

    /**
     * Returns an assembly entry with a name-value pair surrounded by brackets.
     *
     * @param name  the name of the assembly entry
     * @param value the value of the assembly entry
     * @return an assembly entry with a name-value pair surrounded by brackets
     */
    private String createEntry( String name, String value )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "[assembly: Assembly" ).append( name ).append( "(\"" ).append( value ).append( "\")]" ).append(
            "\r\n" );
        return sb.toString();
    }

    private String createCustomStringEntry( String name, String value )
    {
        return "[assembly: CustomStringAttribute(\"" + name + "\", \"" + value + "\")]\r\n";
    }
}
