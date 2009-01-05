package org.apache.maven.plugin.vstudio.xml;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.vstudio.ProjectSettings;
import org.codehaus.plexus.util.xml.XMLWriter;


/**
 * <Config
 * Name = "Debug"
 * AllowUnsafeBlocks = "false"
 * BaseAddress = "285212672"
 * CheckForOverflowUnderflow = "false"
 * ConfigurationOverrideFile = ""
 * DefineConstants = "DEBUG;TRACE"
 * DocumentationFile = ""
 * DebugSymbols = "true"
 * FileAlignment = "4096"
 * IncrementalBuild = "false"
 * NoStdLib = "false"
 * NoWarn = ""
 * Optimize = "false"
 * OutputPath = "bin\Debug\"
 * RegisterForComInterop = "false"
 * RemoveIntegerChecks = "false"
 * TreatWarningsAsErrors = "false"
 * WarningLevel = "4"
 * />
 *
 * @author stevenc
 */
public class VisualStudio2003ConfigWriter
{

    private Log log = null;

    public VisualStudio2003ConfigWriter( Log log )
    {
        this.log = log;
    }

    public void write( XMLWriter writer, ProjectSettings settings )
    {

        writer.startElement( "Config" );
        writer.addAttribute( "Name", "Debug" );
        writer.addAttribute( "AllowUnsafeBlocks", "false" );
        //writer.addAttribute("BaseAddress", "285212672" );
        writer.addAttribute( "CheckForOverflowUnderflow", "false" );
        writer.addAttribute( "ConfigurationOverrideFile", "" );
        writer.addAttribute( "DefineConstants", "DEBUG;TRACE" );
        writer.addAttribute( "DocumentationFile", "" );
        writer.addAttribute( "DebugSymbols", "true" );
        writer.addAttribute( "FileAlignment", "4096" );
        writer.addAttribute( "IncrementalBuild", "false" );
        writer.addAttribute( "NoStdLib", "false" );
        writer.addAttribute( "NoWarn", "" );
        writer.addAttribute( "Optimize", "false" );
        writer.addAttribute( "OutputPath", "target\\dotnet-assembly\\" );
        writer.addAttribute( "RegisterForComInterop", "false" );
        writer.addAttribute( "RemoveIntegerChecks", "false" );
        writer.addAttribute( "TreatWarningsAsErrors", "false" );
        writer.addAttribute( "WarningLevel", "4" );

        writer.endElement();

        writer.startElement( "Config" );
        writer.addAttribute( "Name", "Release" );
        writer.addAttribute( "AllowUnsafeBlocks", "false" );
        //writer.addAttribute("BaseAddress", "285212672" );
        writer.addAttribute( "CheckForOverflowUnderflow", "false" );
        writer.addAttribute( "ConfigurationOverrideFile", "" );
        writer.addAttribute( "DefineConstants", "TRACE" );
        writer.addAttribute( "DocumentationFile", settings.getProjectName() + ".xml" );
        writer.addAttribute( "DebugSymbols", "false" );
        writer.addAttribute( "FileAlignment", "4096" );
        writer.addAttribute( "IncrementalBuild", "false" );
        writer.addAttribute( "NoStdLib", "false" );
        writer.addAttribute( "NoWarn", "" );
        writer.addAttribute( "Optimize", "true" );
        writer.addAttribute( "OutputPath", "target\\dotnet-assembly\\" );
        writer.addAttribute( "RegisterForComInterop", "false" );
        writer.addAttribute( "RemoveIntegerChecks", "false" );
        writer.addAttribute( "TreatWarningsAsErrors", "false" );
        writer.addAttribute( "WarningLevel", "4" );

        writer.endElement();
    }

}

