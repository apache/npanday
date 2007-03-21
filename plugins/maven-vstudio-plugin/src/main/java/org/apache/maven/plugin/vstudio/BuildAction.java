package org.apache.maven.plugin.vstudio;

public class BuildAction
{

    private String type = null;

    private BuildAction( String type )
    {
        this.type = type;
    }

    private static String __BuildActionNone = "None";

    private static String __BuildActionCompile = "Compile";

    private static String __BuildActionContent = "Content";

    private static String __BuildActionEmbeddedResources = "EmbeddedResource";

    public static BuildAction None = new BuildAction( __BuildActionNone );

    public static BuildAction Compile = new BuildAction( __BuildActionCompile );

    public static BuildAction Content = new BuildAction( __BuildActionContent );

    public static BuildAction EmbeddedResources = new BuildAction( __BuildActionEmbeddedResources );

    public String toString()
    {
        return type;
    }

    public boolean equals( Object arg0 )
    {
        if ( arg0 instanceof BuildAction && ( (BuildAction) arg0 ).type.equals( this.type ) )
        {
            return true;
        }
        return false;
    }


}
