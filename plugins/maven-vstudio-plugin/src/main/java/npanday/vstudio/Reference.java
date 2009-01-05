package org.apache.maven.plugin.vstudio;

public class Reference
{

    private String path = null;

    private boolean gac = false;

    private boolean isAbsolute = false;

    /**
     * @return Returns the gac.
     */
    public boolean getGac()
    {
        return gac;
    }

    /**
     * @param gac The gac to set.
     */
    public void setGac( boolean gac )
    {
        this.gac = gac;
    }

    /**
     * @return Returns whether the path expected to be an absolute path
     */
    public boolean getAbsolute()
    {
        return this.isAbsolute;
    }

    /**
     * @param abs Specifies whether the path is expected to be an absolute path
     */
    public void setAbsolute( boolean abs )
    {
        this.isAbsolute = abs;
    }

    /**
     * @return Returns the path.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param path The path to set.
     */
    public void setPath( String path )
    {
        this.path = path;
    }


}
