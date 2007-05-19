package org.apache.maven.dotnet.plugin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.maven.plugin.MojoExecutionException;

@ConfigurationAppenderAnnotation( targetClassName = "java.lang.String")
public class StringConfigurationAppender
    implements ConfigurationAppender
{
    public void append( Document document, Element element, FieldInfo fieldInfo )
        throws MojoExecutionException
    {
        Object value = fieldInfo.getValue();
        if ( ! ( value instanceof String ) )
        {
            throw new MojoExecutionException( "" );
        }

        Node n1 = document.createElement( fieldInfo.getName());
        n1.setTextContent( (String) fieldInfo.getValue() );
        element.appendChild( n1 );
    }
}
