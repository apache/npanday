package org.apache.maven.dotnet.dao;

public enum ProjectUri
{
    GROUP_ID( "http://maven.apache.org/artifact/groupId", "groupId", false ),

    ARTIFACT_ID( "http://maven.apache.org/artifact/artifactId", "artifactId", false ),

    VERSION( "http://maven.apache.org/artifact/version", "versionValue", false ),

    ARTIFACT_TYPE( "http://maven.apache.org/artifact/artifactType", "artifactType", false ),

    CLASSIFIER( "http://maven.apache.org/artifact/classifier", "classifier", true ),

    IS_RESOLVED( "http://maven.apache.org/artifact/dependency/isResolved", "isResolved", true ),

    ARTIFACT( "http://maven.apache.org/Artifact", "artifact", false ),

    DEPENDENCY( "http://maven.apache.org/artifact/dependency", "dependency", true ),

    PARENT( "http://maven.apache.org/artifact/parent", "parent", true ),    

    VENDOR( "http://maven.apache.org/artifact/requirement/vendor", "vendor", false ),

    FRAMEWORK_VERSION( "http://maven.apache.org/artifact/requirement/frameworkVersion", "frameworkVersion", false );

    private String uri;

    private String bindingName;

    private boolean isOptional;

    ProjectUri( String uri, String bindingName, boolean isOptional )
    {
        this.uri = uri;
        this.bindingName = bindingName;
        this.isOptional = isOptional;
    }

    public String getPredicate()
    {
        return uri;
    }

    public String getObjectBinding()
    {
        return bindingName;
    }

    public boolean isOptional()
    {
        return isOptional;
    }

    public void setOptional( boolean isOptional )
    {
        this.isOptional = isOptional;
    }
}
