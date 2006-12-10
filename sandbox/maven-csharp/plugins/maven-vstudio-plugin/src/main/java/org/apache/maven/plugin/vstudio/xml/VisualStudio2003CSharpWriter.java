package org.apache.maven.plugin.vstudio.xml;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.XMLWriter;

/*
 * This writer caters for the CSHARP section of the .proj file.
 *     
 *     <CSHARP
 *        ProjectType = "Local"
 *       ProductVersion = "7.10.3077"
 *       SchemaVersion = "2.0"
 *       ProjectGuid = "{E6E60F5C-A28D-4B3F-A01C-A334F848B9BE}">
 * 
 */
public class VisualStudio2003CSharpWriter
{

    private Log log = null;

    public VisualStudio2003CSharpWriter( Log log )
    {
        this.log = log;
    }

    public void write( XMLWriter writer, String projectType, String schemaVersion, String productVersion,
                       String projectGuid )
    {

    }


}
