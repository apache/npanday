package npanday.vendor.impl;

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

import npanday.vendor.VendorInfoMatchPolicy;
import npanday.vendor.VendorInfo;
import npanday.vendor.InvalidVersionFormatException;
import org.codehaus.plexus.logging.Logger;

/**
 * Provides factory methods for creating vendor info match policies.
 *
 * @author Shane Isbell
 * @see VendorInfoMatchPolicy
 */
final class MatchPolicyFactory
{
    /**
     * A logger for writing log messages
     */    
    private Logger logger;

    /**
     * Default constructor
     */
    MatchPolicyFactory()
    {
    }

    /**
     * Initialize this factory
     *
     * @param logger the plexus logger
     */
    void init( Logger logger )
    {
        this.logger = logger;
    }


    /**
     * Returns a match policy for a vendor name. The accepted vendor names are: Microsoft, Mono and DotGNU.
     *
     * @param vendorName the name of the vendor to match
     * @return a match policy for a vendor name
     */
    VendorInfoMatchPolicy createVendorNamePolicy( final String vendorName )
    {
        return new VendorInfoMatchPolicy()
        {
            public boolean match( VendorInfo vendorInfo )
            {
                if ( vendorInfo == null )
                {
                    return false;
                }
                return isEqual( vendorInfo.getVendor().getVendorName(), vendorName );
            }
        };
    }

    /**
     * Returns a match policy for a vendor version. In the case of Microsoft, this will be the same as the framework
     * version. In the case of Mono and DotGNU, the framework version and vendor version are different.
     *
     * @param vendorVersion the vendor version to match
     * @return a match policy for a vendor version
     */
    VendorInfoMatchPolicy createVendorVersionPolicy( final String vendorVersion )
    {
        return new VendorInfoMatchPolicy()
        {
            public boolean match( VendorInfo vendorInfo )
            {
                if ( vendorInfo == null )
                {
                    return false;
                }
                return isEqual( vendorInfo.getVendorVersion(), vendorVersion );
            }
        };
    }

    /**
     * Returns a match policy for the .NET framework version.
     *
     * @param frameworkVersion the .NET framework version to use for matching vendor info objects
     * @return a match policy for the .NET framework version
     */
    VendorInfoMatchPolicy createFrameworkVersionPolicy( final String frameworkVersion )
    {
        return new VendorInfoMatchPolicy()
        {
            public boolean match( VendorInfo vendorInfo )
            {
                if ( vendorInfo == null )
                {
                    return false;
                }
                VersionMatcher versionMatcher = new VersionMatcher();
                try
                {
                    return versionMatcher.matchVersion( frameworkVersion, vendorInfo.getFrameworkVersion() );
                }
                catch ( InvalidVersionFormatException e )
                {
                    logger.info( "NPANDAY-101-000: Invalid framework version: Version = " + frameworkVersion, e );
                    return false;
                }
            }
        };
    }

    /**
     * Returns a vendor info match policy for matching whether a vendor info is a default entry. The vendor info
     * match policy's <code>match</code> method will return true if the vendor info is not null and is a
     * default entry, otherwise it will return false.
     *
     * @return a vendor info match policy for matching whether a vendor info is a default entry
     */
    VendorInfoMatchPolicy createVendorIsDefaultPolicy()
    {
        return new VendorInfoMatchPolicy()
        {
            public boolean match( VendorInfo vendorInfo )
            {
                if ( vendorInfo == null )
                {
                    return false;
                }
                return vendorInfo.isDefault();
            }
        };
    }

    /**
     * Returns true if the first value equals the second value, otherwise returns false. This comparison is <i>not</i>
     * case or white-space sensitive. Null values will be treated as an empty string, so if the first value is null and
     * the second value is empty (or only contains white-space), this method will return true.
     *
     * @param value  the first value in the comparison
     * @param value1 the second value in the comparison
     * @return true if the first value equals the second value, otherwise returns false.
     */
    private boolean isEqual( String value, String value1 )
    {
        return normalize( value ).equals( normalize( value1 ) );
    }

    /**
     * Normalizes the specified value by 1) making it all lower case and 2) removing all white-space. A null value will
     * be treated as an empty string.
     *
     * @param value the value to normalize
     * @return a normalized value that is lower case with no white-space
     */
    private String normalize( String value )
    {
        return ( value != null ) ? value.toLowerCase().trim() : "";
    }
}
