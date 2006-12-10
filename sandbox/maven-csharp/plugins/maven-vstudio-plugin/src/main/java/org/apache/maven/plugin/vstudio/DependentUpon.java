package org.apache.maven.plugin.vstudio;

public class DependentUpon
{

    private String fileName = null;

    public DependentUpon( String fileName )
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String toString()
    {
        return fileName;
    }
}
