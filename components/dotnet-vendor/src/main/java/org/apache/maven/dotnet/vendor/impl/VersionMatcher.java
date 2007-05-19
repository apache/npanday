/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.dotnet.vendor.impl;

import java.util.regex.Pattern;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.dotnet.vendor.InvalidVersionFormatException;

/**
 * Provides a way to match versions.
 *
 * @author Shane Isbell
 */
final class VersionMatcher
{
    /**
     * A pattern for a valid version format: \p{Alnum}[._-]]*[+*]?. This will be used for determining whether a
     * version is valid.
     */
    private static Pattern versionIdMatch = Pattern.compile( "[\\p{Alnum}[._-]]*[+*]?" );

    /**
     * Constant denoting no modifier
     */
    private final static int nullModifier = 0;

    /**
     * Constant for '+' modifier
     */
    private final static int plusModifier = 1;

    /**
     * Constant for '*' modifier
     */
    private final static int starModifier = 2;

    /**
     * Default constructor
     */
    VersionMatcher()
    {
    }

    /**
     * Returns true if the specified parameter versions match, otherwise returns false. [Give full rules here].
     *
     * @param req the required version
     * @param cap the capability version
     * @return true if the specified versions (req and cap) match
     * @throws InvalidVersionFormatException if the specified parameters contain an invalid version format
     */
    boolean matchVersion( String req, String cap )
        throws InvalidVersionFormatException
    {
        String[] requirement = tokenizeVersion( req );
        String[] capability = tokenizeVersion( cap );

        int capSize = capability.length;
        int reqSize = requirement.length;

        int reqModifier = getModifier( requirement );
        if ( reqModifier == plusModifier )
        {
            requirement[reqSize - 1] = requirement[reqSize - 1].replace( '+', ' ' ).trim();
        }
        else if ( reqModifier == starModifier )
        {
            requirement[reqSize - 1] = requirement[reqSize - 1].replace( '*', ' ' ).trim();
        }

        if ( reqSize < capSize && reqModifier != starModifier )
        {
            requirement = padArray( requirement, capSize );
        }
        else if ( capSize < reqSize )
        {
            capability = padArray( capability, reqSize );
        }

        switch ( reqModifier )
        {
            case nullModifier:
                return testExactMatch( requirement, capability );
            case plusModifier:
                return testGreaterThanMatch( requirement, capability );
            case starModifier:
                return testPrefixMatch( requirement, capability );
            default:
                return false;
        }
    }

    /**
     * Returns the maximum version of the given set of versions.
     *
     * @param versions a set of versions from which to choose the maximum version
     * @return the maximum version from the specified set of versions.
     * @throws InvalidVersionFormatException if the format of one or more of the versions is invalid
     */
    String getMaxVersion( Set<String> versions )
        throws InvalidVersionFormatException
    {
        if ( versions.isEmpty() )
        {
            return null;
        }
        Iterator i = versions.iterator();
        String maxVersion = (String) i.next();
        while ( i.hasNext() )
        {
            String testValue = (String) i.next();
            if ( isGreaterThan(maxVersion, testValue ) )
            {
                maxVersion = testValue;
            }
        }
        return maxVersion;
    }

    String getMinVersion( Set<String> versions )
        throws InvalidVersionFormatException
    {
        if ( versions.isEmpty() )
        {
            return null;
        }
        Iterator i = versions.iterator();
        String minVersion = (String) i.next();
        while ( i.hasNext() )
        {
            String testValue = (String) i.next();
            if ( isGreaterThan( testValue, minVersion ) )
            {
                minVersion = testValue;
            }
        }
        return minVersion;
    }

    /**
     * Returns true if v > v1, otherwise returns false.
     *
     * @param v     the first value to compare
     * @param v1    the second value to compare
     * @return true if v > v1, otherwise returns false
     */
    private boolean isGreaterThan( String v, String v1 )
        throws InvalidVersionFormatException
    {
        String[] requirement = tokenizeVersion( v );
        String[] capability = tokenizeVersion( v1 );

        int capSize = capability.length;
        int reqSize = requirement.length;
        if ( reqSize < capSize )
        {
            requirement = padArray( requirement, capSize );
        }
        else if ( capSize < reqSize )
        {
            capability = padArray( capability, reqSize );
        }

        return testGreaterThanMatch( requirement, capability );
    }

    /**
     * Returns an array padded with zeros. This is needed because one version may contain, say 1.2, while another version may
     * be 1.2.8. We need to add the implied 0 to read 1.2.0 so that the versions can be compared.
     *
     * @param value a string array without padded values
     * @param size  the size that the value array needs to be expanded
     * @return a string array with padded values
     */
    private String[] padArray( String[] value, int size )
    {
        int valueSize = value.length;
        int padSize = Math.abs( valueSize - size );

        String[] newValue = new String[size];

        System.arraycopy( value, 0, newValue, 0, valueSize );
        for ( int i = 0; i < padSize; i++ )
        {
            newValue[i + valueSize] = "0";
        }
        return newValue;
    }

    /**
     * Returns an int denoting a modifier (+, *) within the version. A value of 0 means that there is no modifier, a
     * value of 1 means that there is a + modifier and a value of 2 means that there is a * modifier.
     *
     * @param value a tokenized string array of the version
     * @return an int denoting a modifier within the version
     */
    private int getModifier( String[] value )
    {
        String lastValue = value[value.length - 1].trim();
        char lastChar = lastValue.charAt( lastValue.length() - 1 );
        if ( lastChar == '+' )
        {
            return plusModifier;
        }
        else if ( lastChar == '*' )
        {
            return starModifier;
        }
        else
        {
            return nullModifier;
        }
    }

    /**
     * Returns true if the requirement parameter exactly matches (each array value is the same) the capability.
     *
     * @param requirement   the requirement to match
     * @param capability    the capability to match
     * @return true if the requirement parameter exactly matches (each array value is the same) the capability
     */
    private boolean testExactMatch( String[] requirement, String[] capability )
    {
        int reqSize = requirement.length;
        if ( reqSize != capability.length )
        {
            return false;
        }

        for ( int i = 0; i < reqSize; i++ )
        {
            if ( !requirement[i].equals( capability[i] ) )
            {
                return false;
            }
        }
        return true;
    }

    private boolean testGreaterThanMatch( String[] requirement, String[] capability )
    {
        int reqSize = requirement.length;
        for ( int i = 0; i < reqSize; i++ )
        {
            if ( isNumber( requirement[i] ) && isNumber( capability[i] ) )
            {
                try
                {
                    int compare = new Integer( capability[i] ).compareTo( new Integer( requirement[i] ) );
                    if ( compare < 0 )
                    {
                        return false;
                    }
                    if ( compare > 0 )
                    {
                        return true;
                    }
                }
                catch ( NumberFormatException e )
                {
                    //this should never happen: already done check
                }
            }
            else
            {
                for ( int j = 0; j < reqSize - 1; j++ )
                {
                    char req = requirement[i].charAt( j );
                    char cap = capability[i].charAt( j );
                    if ( req < cap )
                    {
                        return true;
                    }
                    if ( req > cap )
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the specified number parameter is an integer, otherwise returns false.
     *
     * @param number the number to test for an integer format
     * @return true if the specified number parameter is an integer, otherwise returns false
     */
    private boolean isNumber( String number )
    {
        try
        {
            new Integer( number );
            return true;
        }
        catch ( NumberFormatException e )
        {
            return false;
        }
    }

    private boolean testPrefixMatch( String[] requirement, String[] capability )
    {
        int reqSize = requirement.length;
        for ( int i = 0; i < reqSize; i++ )
        {
            if ( !requirement[i].equals( capability[i] ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a tokenized string array of the version based on the following standard version delimiters: '.', '_', '-'.
     *
     * @param version the version to tokenize
     * @return a tokenized string array of the version based on the following standard version delimiters: '.', '_', '-'
     * @throws InvalidVersionFormatException if the version format is invalid
     */
    private String[] tokenizeVersion( String version )
        throws InvalidVersionFormatException
    {
        if ( !isVersionId( version ) )
        {
            throw new InvalidVersionFormatException( "Invalid Version Id: ID = " + version );
        }
        return version.split( "[._-]" );
    }

    /**
     * Returns true if the specified version is valid (\p{Alnum}[._-]]*[+*]?), otherwise returns false.
     *
     * @param version the version to test for validity
     * @return true if the specified version is valid (\p{Alnum}[._-]]*[+*]?), otherwise returns false
     */
    private boolean isVersionId( String version )
    {
        return ( version != null ) && versionIdMatch.matcher( version ).matches();
    }

}
