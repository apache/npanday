package org.apache.maven.plugin.nunit;

import org.codehaus.plexus.util.StringUtils;

/**
 * Wrapper class for the systemPropery argument type.
 */

public class Property
{
    private String key;

    private String value;

    public String getKey()
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( StringUtils.defaultString( this.key, "NULL" ) );
        sb.append( ":" );
        sb.append( StringUtils.defaultString( this.value, "NULL" ) );
        return sb.toString();
    }
}
