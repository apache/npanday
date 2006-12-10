package org.apache.maven.plugin.vstudio.xml;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.XMLWriter;

public class VisualStudio2003WebInfoWriter
{

    private Log log = null;

    public VisualStudio2003WebInfoWriter( Log log )
    {
        this.log = log;
    }

    public void write( XMLWriter writer, String webProjectUrl )
    {

        writer.startElement( "VisualStudioUNCWeb" );

        writer.startElement( "Web" );
        writer.addAttribute( "URLPath", webProjectUrl );

        writer.endElement(); //end Web

        writer.endElement();//end VisualStudioUNCWeb
    }
}
