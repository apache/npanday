package org.apache.maven.plugin.vstudio.xml;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.vstudio.ProjectSettings;
import org.apache.maven.plugin.vstudio.VisualStudioFile;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.util.Set;

/**
 * <VisualStudioProject>
 * <CSHARP
 * ProjectType = "Local"
 * ProductVersion = "7.10.3077"
 * SchemaVersion = "2.0"
 * ProjectGuid = "{E6E60F5C-A28D-4B3F-A01C-A334F848B9BE}"
 * >
 * </VisualStudioProject>
 *
 * @author stevenc
 */
public class VisualStudio2003Writer
{

    private Log log = null;

    public VisualStudio2003Writer( Log log )
    {
        this.log = log;
    }

    public void write( XMLWriter writer, ProjectSettings settings, VisualStudioFile[] sources,
                       VisualStudioFile[] testsources, VisualStudioFile[] resources, Set artifacts, File frameworkDir,
                       VisualStudioFile pom, Set references )
    {

        writer.startElement( "VisualStudioProject" );

        writer.startElement( "CSHARP" );

        writer.addAttribute( "ProjectType", settings.getProjectType() );
        writer.addAttribute( "ProductVersion", settings.getProductVersion() );
        writer.addAttribute( "SchemaVersion", settings.getSchemaVersion() );
        //writer.addAttribute("ProjectGuid", settings.getProjectGuid() );

        writer.startElement( "Build" );

        new VisualStudio2003SettingsWriter( this.log ).write( writer, settings );

        new VisualStudio2003ConfigWriter( this.log ).write( writer, settings );

        new VisualStudio2003ReferenceWriter( this.log ).write( writer, artifacts, references, frameworkDir );

        writer.endElement();//end Build

        new VisualStudio2003FileWriter( this.log ).write( writer, settings, sources, testsources, resources, pom );

        writer.endElement();//end CSHARP

        writer.endElement();//end VisualStudioProject
    }
}
