package org.apache.maven.plugin.vstudio.xml;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.vstudio.ProjectSettings;
import org.codehaus.plexus.util.xml.XMLWriter;


/**
 * <Settings
 * ApplicationIcon = ""
 * AssemblyKeyContainerName = ""
 * AssemblyName = "DrKW.CPDS.Proxy.Authorisation"
 * AssemblyOriginatorKeyFile = ""
 * DefaultClientScript = "JScript"
 * DefaultHTMLPageLayout = "Grid"
 * DefaultTargetSchema = "IE50"
 * DelaySign = "false"
 * OutputType = "Library"
 * PreBuildEvent = ""
 * PostBuildEvent = ""
 * RootNamespace = "DrKW.CPDS.Proxy.Authorisation"
 * RunPostBuildEvent = "OnBuildSuccess"
 * StartupObject = ""
 * >
 *
 * @author stevenc
 */
public class VisualStudio2003SettingsWriter
{

    private Log log = null;

    public VisualStudio2003SettingsWriter( Log log )
    {

    }

    public void write( XMLWriter writer, ProjectSettings settings )
    {

        writer.startElement( "Settings" );
        writer.addAttribute( "ApplicationIcon", settings.getApplicationIcon() );
        writer.addAttribute( "AssemblyKeyContainerName", settings.getAssemblyKeyContainerName() );
        writer.addAttribute( "AssemblyName", settings.getAssemblyName() );
        writer.addAttribute( "AssemblyOriginatorKeyFile", settings.getAssemblyOriginatorKeyFile() );
        writer.addAttribute( "DefaultClientScript", settings.getDefaultClientScript() );
        writer.addAttribute( "DefaultHTMLPageLayout", settings.getDefaultHTMLPageLayout() );
        writer.addAttribute( "DefaultTargetSchema", settings.getDefaultTargetSchema() );
        writer.addAttribute( "DelaySign", settings.getDelaySign() );
        writer.addAttribute( "OutputType", settings.getOutputType() );
        writer.addAttribute( "PreBuildEvent", settings.getPreBuildEvent() );
        writer.addAttribute( "PostBuildEvent", settings.getPostBuildEvent() );
        writer.addAttribute( "RootNamespace", settings.getRootNamespace() );
        writer.addAttribute( "RunPostBuildEvent", settings.getRunPostBuildEvent() );
        writer.addAttribute( "StartupObject", settings.getStartupObject() );

        writer.endElement();

    }


}

