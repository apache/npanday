package org.apache.maven.plugin.vstudio.xml;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.vstudio.ProjectSettings;
import org.apache.maven.plugin.vstudio.SubType;
import org.apache.maven.plugin.vstudio.VisualStudioFile;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.XMLWriter;

public class VisualStudio2003FileWriter
{

    private Log log = null;

    public VisualStudio2003FileWriter( Log log )
    {
        this.log = log;
    }

    public void write( XMLWriter writer, ProjectSettings settings, VisualStudioFile[] source,
                       VisualStudioFile[] testSource, VisualStudioFile[] resources, VisualStudioFile pom )
    {

        writer.startElement( "Files" );
        writer.startElement( "Include" );

        log.debug( "----------------Adding files-------------------" );

        //-----------------------------------------
        //write out sources to .proj file.
        //-----------------------------------------

        for ( int i = 0; i < source.length; i++ )
        {

            log.info( "Adding source:" + source[i].getRelativePath() );

            writer.startElement( "File" );
            writer.addAttribute( "RelPath", source[i].getRelativePath() );

            if ( ( source[i].getDependent() != null ) &&
                ( !StringUtils.isEmpty( source[i].getDependent().getFileName() ) ) )
            {
                writer.addAttribute( "DependentUpon", source[i].getDependent().getFileName() );
            }

            if ( source[i].getSub() != null && ( !source[i].getSub().equals( SubType.Null ) ) )
            {
                writer.addAttribute( "SubType", source[i].getSub().toString() );
            }

            writer.addAttribute( "BuildAction", source[i].getAction().toString() );
            writer.endElement();
        }

        //-----------------------------------------
        //write out test sources to .proj file.
        //-----------------------------------------

        for ( int i = 0; i < testSource.length; i++ )
        {

            log.info( "Adding test source:" + testSource[i].getRelativePath() );

            writer.startElement( "File" );
            writer.addAttribute( "RelPath", testSource[i].getRelativePath() );

            if ( !testSource[i].getSub().equals( SubType.Null ) )
            {
                writer.addAttribute( "SubType", testSource[i].getSub().toString() );
            }

            writer.addAttribute( "BuildAction", testSource[i].getAction().toString() );
            writer.endElement();
        }

        //-----------------------------------------
        //write out resources to .proj file.
        //-----------------------------------------

        for ( int i = 0; i < resources.length; i++ )
        {

            log.info( "Adding resource:" + resources[i].getRelativePath() );

            writer.startElement( "File" );
            writer.addAttribute( "RelPath", resources[i].getRelativePath() );

            if ( !resources[i].getSub().equals( SubType.Null ) )
            {
                writer.addAttribute( "SubType", resources[i].getSub().toString() );
            }

            writer.addAttribute( "BuildAction", resources[i].getAction().toString() );
            writer.endElement();
        }

        log.info( "Adding pom:" + pom.getRelativePath() );

        writer.startElement( "File" );
        writer.addAttribute( "RelPath", pom.getRelativePath() );

        if ( !pom.getSub().equals( SubType.Null ) )
        {
            writer.addAttribute( "SubType", pom.getSub().toString() );
        }

        writer.addAttribute( "BuildAction", pom.getAction().toString() );
        writer.endElement();

        //close Files and Includes elements
        writer.endElement();
        writer.endElement();
    }
}

