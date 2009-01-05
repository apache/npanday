package org.apache.maven.plugin.vstudio;

import java.util.ArrayList;
import java.util.List;

public class References
{

    private List references = new ArrayList();

    /**
     * @return Returns the references.
     */
    public List getReferences()
    {
        return references;
    }

    /**
     * @param references The references to set.
     */
    public void setReferences( List references )
    {
        this.references = references;
    }

    public void addReference( Reference ref )
    {
        this.references.add( ref );
    }

}
