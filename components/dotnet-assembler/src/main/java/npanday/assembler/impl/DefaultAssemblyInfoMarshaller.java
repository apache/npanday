package npanday.assembler.impl;

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

import npanday.assembler.AssemblyInfoException;
import npanday.assembler.AssemblyInfoMarshaller;
import npanday.assembler.AssemblyInfo;
import npanday.assembler.AssemblyInfo.TargetFramework;
import npanday.model.assembly.plugins.AssemblyPlugin;

import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
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
        throws AssemblyInfoException, IOException
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "using System.Reflection;\r\n" )
            .append( "using System.Runtime.CompilerServices;\r\n" );
        appendEntry( sb, "Description", assemblyInfo.getDescription() );
        appendEntry( sb, "Title", assemblyInfo.getTitle() );
        appendEntry( sb, "Company", assemblyInfo.getCompany() );
        appendEntry( sb, "Product", assemblyInfo.getProduct() );
        if (assemblyInfo.getCopyright() != null)
        {
            appendEntry( sb, "Copyright", assemblyInfo.getCopyright().replace( "\"", "\\" ) );
        }
        appendEntry( sb, "Trademark", assemblyInfo.getTrademark() );
        appendEntry( sb, "Culture", assemblyInfo.getCulture() );
        appendEntry( sb, "Version", assemblyInfo.getVersion() );
        appendEntry( sb, "InformationalVersion", assemblyInfo.getInformationalVersion() );
        appendEntry( sb, "Configuration", assemblyInfo.getConfiguration() );
        appendEntry( sb, "KeyName", assemblyInfo.getKeyName() );

        if ( assemblyInfo.getKeyFile() != null )
        {
            appendEntry( sb, "KeyFile", assemblyInfo.getKeyFile().getAbsolutePath().replace( "\\", "\\\\" ) );
        }

        TargetFramework targetFramework = assemblyInfo.getTargetFramework();
        if (targetFramework != null)
        {
            String frameworkName = targetFramework.getFrameworkName();
            String frameworkDisplayName = targetFramework.getFrameworkDisplayName();
            sb.append( "[assembly: global::System.Runtime.Versioning.TargetFrameworkAttribute" )
              .append( "(\"" ).append( frameworkName ).append("\"");
            if (frameworkDisplayName != null)
            {
                sb.append(",FrameworkDisplayName=\"").append(frameworkDisplayName).append("\"");
            }
            sb.append( ")]" ).append("\r\n" );
        }

        for(Entry<String, String> e: assemblyInfo.getAssemblyAttributes().entrySet()) {
            if(StringUtils.isEmpty(e.getValue()))
                continue;

            // get all values per key (e.g. "A;B;C")
            String valuesPerElement = e.getValue();
            
            StringTokenizer st = new StringTokenizer(valuesPerElement, ";");
            
            while (st.hasMoreTokens()) {
                // each value will be assigned to the enclosing key/element
                sb.append( "[assembly: ")
                .append(e.getKey())
                .append("(\"")
                .append(st.nextToken())
                .append("\")]")
                .append("\r\n" );
            }
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
            if ( outputStream == null )
            {
                String src = mavenProject.getBuild().getDirectory() + "/build-sources";
                String groupIdAsDir = mavenProject.getGroupId().replace( ".", File.separator );
                File file = new File( src + "/META-INF/" + groupIdAsDir );
                file.mkdirs();
                man = new FileOutputStream(
                    src + "/META-INF/" + groupIdAsDir + File.separator + "AssemblyInfo." + plugin.getExtension() );
                outputStream = man;
            }
            outputStream.write( sb.toString().getBytes() );
        }
        catch ( IOException e )
        {
            throw new AssemblyInfoException( "NPANDAY-022-000: Failed to generate AssemblyInfo", e );
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
        throws IOException, AssemblyInfoException
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
        throws AssemblyInfoException
    {
        if ( !name.startsWith( "Assembly" ) )
        {
            throw new AssemblyInfoException(
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
     * Appends an assembly entry with a name-value pair surrounded by brackets.
     *
     * @param sb    the string buffer to be appended
     * @param name  the name of the assembly entry
     * @param value the value of the assembly entry
     */
    private void appendEntry( StringBuffer sb, String name, String value )
    {
        if (value != null)
        {
            sb.append( "[assembly: Assembly" ).append( name ).append( "(\"" ).append( value ).append( "\")]" ).append(
                "\r\n" );
        }
    }

    private String createCustomStringEntry( String name, String value )
    {
        return "[assembly: CustomStringAttribute(\"" + name + "\", \"" + value + "\")]\r\n";
    }
}
