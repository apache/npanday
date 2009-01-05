package org.apache.maven.plugin.vstudio;

public class ProjectSettings
{

    /* Settings inside the build element */
    private String rootNamespace = "";

    private String applicationIcon = "";

    private String assemblyKeyContainerName = "";

    private String assemblyName = "";

    private String assemblyOriginatorKeyFile = "";

    private String defaultClientScript = "JScript";

    private String defaultHTMLPageLayout = "Grid";

    private String defaultTargetSchema = "IE50";

    private String delaySign = "false";

    private String outputType = "Library";

    private String preBuildEvent = "";

    private String postBuildEvent = "";

    private String runPostBuildEvent = "OnBuildSuccess";

    private String startupObject = "";

    private String projectName = "";

    private String projectType = "Local";

    private String productVersion = "7.10.3077";

    private String schemaVersion = "2.0";

    private String projectGuid = "{E6E60F5C-A28D-4B3F-A01C-A334F848B9BE}";

    /**
     * if this project has a packaging type dotnet-web
     * then this parameter must be supplied so that the system can write out the
     * .csproj.webinfo file as well as the .csproj file.
     */
    private String webProjectUrlPath = null;


    public ProjectSettings()
    {
    }


    public String getProductVersion()
    {
        return productVersion;
    }

    public void setProductVersion( String productVersion )
    {
        this.productVersion = productVersion;
    }

    public String getProjectGuid()
    {
        return projectGuid;
    }

    public void setProjectGuid( String projectGuid )
    {
        this.projectGuid = projectGuid;
    }

    public String getProjectType()
    {
        return projectType;
    }

    public void setProjectType( String projectType )
    {
        this.projectType = projectType;
    }

    public String getSchemaVersion()
    {
        return schemaVersion;
    }

    public void setSchemaVersion( String schemaVersion )
    {
        this.schemaVersion = schemaVersion;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public String getApplicationIcon()
    {
        return applicationIcon;
    }

    public void setApplicationIcon( String applicationIcon )
    {
        this.applicationIcon = applicationIcon;
    }

    public String getAssemblyKeyContainerName()
    {
        return assemblyKeyContainerName;
    }

    public void setAssemblyKeyContainerName( String assemblyKeyContainerName )
    {
        this.assemblyKeyContainerName = assemblyKeyContainerName;
    }

    public String getAssemblyName()
    {
        return assemblyName;
    }

    public void setAssemblyName( String assemblyName )
    {
        this.assemblyName = assemblyName;
    }

    public String getAssemblyOriginatorKeyFile()
    {
        return assemblyOriginatorKeyFile;
    }

    public void setAssemblyOriginatorKeyFile( String assemblyOriginatorKeyFile )
    {
        this.assemblyOriginatorKeyFile = assemblyOriginatorKeyFile;
    }

    public String getDefaultClientScript()
    {
        return defaultClientScript;
    }

    public void setDefaultClientScript( String defaultClientScript )
    {
        this.defaultClientScript = defaultClientScript;
    }

    public String getDefaultHTMLPageLayout()
    {
        return defaultHTMLPageLayout;
    }

    public void setDefaultHTMLPageLayout( String defaultHTMLPageLayout )
    {
        this.defaultHTMLPageLayout = defaultHTMLPageLayout;
    }

    public String getDefaultTargetSchema()
    {
        return defaultTargetSchema;
    }

    public void setDefaultTargetSchema( String defaultTargetSchema )
    {
        this.defaultTargetSchema = defaultTargetSchema;
    }

    public String getDelaySign()
    {
        return delaySign;
    }

    public void setDelaySign( String delaySign )
    {
        this.delaySign = delaySign;
    }

    public String getOutputType()
    {
        return outputType;
    }

    public void setOutputType( String outputType )
    {
        this.outputType = outputType;
    }

    public String getPostBuildEvent()
    {
        return postBuildEvent;
    }

    public void setPostBuildEvent( String postBuildEvent )
    {
        this.postBuildEvent = postBuildEvent;
    }

    public String getPreBuildEvent()
    {
        return preBuildEvent;
    }

    public void setPreBuildEvent( String preBuildEvent )
    {
        this.preBuildEvent = preBuildEvent;
    }

    public String getRootNamespace()
    {
        return rootNamespace;
    }

    public void setRootNamespace( String rootNamespace )
    {
        this.rootNamespace = rootNamespace;
    }

    public String getRunPostBuildEvent()
    {
        return runPostBuildEvent;
    }

    public void setRunPostBuildEvent( String runPostBuildEvent )
    {
        this.runPostBuildEvent = runPostBuildEvent;
    }

    public String getStartupObject()
    {
        return startupObject;
    }

    public void setStartupObject( String startupObject )
    {
        this.startupObject = startupObject;
    }

    public String getWebProjectUrlPath()
    {
        return webProjectUrlPath;
    }

    public void setWebProjectUrlPath( String webProjectUrlPath )
    {
        this.webProjectUrlPath = webProjectUrlPath;
    }
}

