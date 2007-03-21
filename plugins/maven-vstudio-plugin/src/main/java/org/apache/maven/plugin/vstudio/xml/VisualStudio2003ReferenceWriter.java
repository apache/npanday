package org.apache.maven.plugin.vstudio.xml;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

public class VisualStudio2003ReferenceWriter
{

    private Log log = null;

    /**
     * <Reference
     * Name = "System"
     * AssemblyName = "System"
     * HintPath = "..\..\..\..\..\..\..\..\WINDOWS\Microsoft.NET\Framework\v1.1.4322\System.dll"
     * />
     *
     * @param log
     */
    public VisualStudio2003ReferenceWriter( Log log )
    {
        this.log = log;
    }

    public void write( XMLWriter writer, Set artifacts, Set references, File frameworkHome )
    {

        writer.startElement( "References" );

        log.info( "-------------------Adding references---------------------" );

        log.info( "-------------------Adding implicit references------------" );

        for ( Iterator i = artifacts.iterator(); i.hasNext(); )
        {
            Artifact a = (Artifact) i.next();
            writeArtifact( writer, a, frameworkHome );
        }

        log.info( "-------------------Adding explicit references------------" );

        for ( Iterator i = references.iterator(); i.hasNext(); )
        {
            Artifact a = (Artifact) i.next();
            writeArtifact( writer, a, frameworkHome );
        }

        log.info( "-------------------Done------------------------" );

        writer.endElement();
    }

    private void writeArtifact( XMLWriter writer, Artifact a, File frameworkHome )
    {

        //if it is a dotnet assembly and not a jar etc...
        if ( isDotnet( a ) )
        {

            if ( a.getScope().equals( Artifact.SCOPE_PROVIDED ) )
            {
                writeProvidedReference( writer, a, frameworkHome );
            }
            else
            {
                writeNormalReference( writer, a );
            }

        }
        else
        {
            log.info( "Ignoring ref:" + a.getArtifactId() + " as is non-dotnet type [" + a.getType() + "]" );
        }
    }

    public void writeNormalReference( XMLWriter writer, Artifact a )
    {

        String hintPath = a.getFile().getAbsolutePath();
        String assemblyName = FilenameUtils.removeExtension( a.getFile().getName() );
        String name = assemblyName;

        log.info( "Adding Ref:" + a.getArtifactId() + "." + a.getType() + " from " + hintPath );

        writer.startElement( "Reference" );
        writer.addAttribute( "Name", name );
        writer.addAttribute( "AssemblyName", assemblyName );
        writer.addAttribute( "HintPath", hintPath );
        writer.endElement();
    }

    public void writeProvidedReference( XMLWriter writer, Artifact a, File frameworkHome )
    {

        String assemblyName = FilenameUtils.removeExtension( a.getFile().getName() );
        String name = assemblyName;

        if ( a.getGroupId().startsWith( "System" ) )
        {

            File f = new File( frameworkHome, a.getArtifactId() + "." + a.getType() );

            log.info( "Adding System Ref:" + a.getArtifactId() + "." + a.getType() + " from " + f.getAbsoluteFile() );

            if ( f.exists() && f.isFile() )
            {

                writer.startElement( "Reference" );

                writer.addAttribute( "Name", name );
                writer.addAttribute( "AssemblyName", assemblyName );
                writer.addAttribute( "HintPath", f.getAbsolutePath() );
                writer.endElement();

            }

        }
        else
        {
            log.info( "Ignorning Ref [scope=provided and groupId not System]:" + a.getArtifactId() + "." + a.getType() +
                " " + a.getFile().getAbsolutePath() );
        }
    }

    private boolean isDotnet( Artifact a )
    {
        if ( a.getType().equals( "dotnet-exe" ) || a.getType().equals( "dotnet-winexe" ) ||
            a.getType().equals( "dotnet-library" ) || a.getType().equals( "dotnet-webapp" ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
