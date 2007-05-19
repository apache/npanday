package org.apache.maven.dotnet.plugin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public class PomFileConfigurationAppender
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

        Node n1 = document.createElement( "pomFile" );
        n1.setTextContent( ((MavenProject) value).getFile().getAbsolutePath() );
        document.appendChild( n1 );
    }
}
