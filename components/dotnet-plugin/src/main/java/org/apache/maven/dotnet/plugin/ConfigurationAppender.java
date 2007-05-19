package org.apache.maven.dotnet.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

public interface ConfigurationAppender
{

    void append( Document document, Element element, FieldInfo fieldInfo) throws MojoExecutionException;

}
