package npanday.plugin;

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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.IOUtil;

import java.io.StringWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

@ConfigurationAppenderAnnotation( targetClassName = "org.apache.maven.project.MavenProject")
public class MavenProjectConfigurationAppender
    implements ConfigurationAppender
{
    public void append( Document document, Element element, FieldInfo fieldInfo )
        throws MojoExecutionException
    {
        Object value = fieldInfo.getValue();
        if ( ! ( value instanceof MavenProject ) )
        {
            throw new MojoExecutionException( "" );
        }
        File mavenProjectFile = writeMavenProjectToTempFile( (MavenProject) value );
        Node mavenProjectFileNode = document.createElement( "mavenProject" );
        mavenProjectFileNode.setTextContent( mavenProjectFile.getAbsolutePath() );
        element.appendChild( mavenProjectFileNode );
    }

    private String ToString( MavenProject mavenProject )
    {
        StringWriter stringWriter = new StringWriter();
        try
        {
            new MavenXpp3Writer().write( stringWriter, mavenProject.getModel() );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtil.close( stringWriter );
        }
        return stringWriter.getBuffer().toString().replaceFirst( "<project>",
                                                                 "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" );
    }

    public File writeMavenProjectToTempFile( MavenProject project )
    {
        String p = ToString( project );
        try
        {
            File tempFile = File.createTempFile( "MavenProject", ".xml" );
            FileOutputStream fos = new FileOutputStream( tempFile );
            fos.write( p.getBytes() );
            fos.close();
            return tempFile;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return null;
    }
}
