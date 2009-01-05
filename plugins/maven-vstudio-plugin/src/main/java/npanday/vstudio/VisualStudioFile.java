package org.apache.maven.plugin.vstudio;

import java.io.File;

public class VisualStudioFile
{

    private File file = null;

    private BuildAction action = null;

    private String relativePath = null;

    private SubType sub = null;

    private DependentUpon dependent = null;

    public VisualStudioFile( File file, BuildAction action, String relativePath, SubType sub )
    {
        this.file = file;
        this.action = action;
        this.relativePath = relativePath;
        this.sub = sub;
    }

    public VisualStudioFile( File file, BuildAction action, String relativePath, SubType sub, DependentUpon dependent )
    {
        this.file = file;
        this.action = action;
        this.relativePath = relativePath;
        this.sub = sub;
        this.dependent = dependent;
    }

    public VisualStudioFile( File file, BuildAction action, String relativePath, DependentUpon dependent )
    {
        this.file = file;
        this.action = action;
        this.relativePath = relativePath;
        this.dependent = dependent;
    }

    public BuildAction getAction()
    {
        return action;
    }

    public File getFile()
    {
        return file;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public SubType getSub()
    {
        return sub;
    }

    public DependentUpon getDependent()
    {
        return dependent;
    }


    public String toString()
    {
        return "VSFile[@rel=" + relativePath + ",@path=" + file.getAbsolutePath() + ",@action=" + action +
            ",@subtype=" + sub + ",@dep=" + dependent + "]";
    }
}
