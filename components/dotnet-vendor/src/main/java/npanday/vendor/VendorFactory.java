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
package npanday.vendor;

import npanday.PlatformUnsupportedException;

/**
 * Provides services for returning an instance of Vendor based on the OS or name.
 *
 * @author Shane Isbell
 */
public final class VendorFactory
{

    /**
     * Class constructor
     */
    private VendorFactory()
    {
    }

    /**
     * Returns the default vendor of the operating system on which the application is running:
     * Microsoft for windows and Mono for all others.
     *
     * @return the default vendor of the operating system on which the application is running
     * @throws npanday.PlatformUnsupportedException
     *          if the default vendor cannot be determined
     */
    public static synchronized Vendor getDefaultVendorForOS()
        throws PlatformUnsupportedException
    {
        return System.getProperty( "os.name" ).toLowerCase().trim().contains( "windows" )
            ? VendorFactory.createVendorFromName( "MICROSOFT" ) : VendorFactory.createVendorFromName( "MONO" );
    }

    /**
     * Returns a vendor instance for the given vendor name: MICROSOFT, DotGNU, MONO.
     *
     * @param vendorName the name of a vendor
     * @return a vendor instance for the given vendor name: MICROSOFT, DotGNU, MONO
     * @throws VendorUnsupportedException if the vendor is not known
     * @throws NullPointerException if the vendor name parameter is null
     */
    public static synchronized Vendor createVendorFromName( String vendorName )
        throws VendorUnsupportedException
    {
        if ( vendorName.toLowerCase().trim().equals( "microsoft" ) )
        {
            return Vendor.MICROSOFT;
        }
        else if ( vendorName.toLowerCase().trim().equals( "mono" ) )
        {
            return Vendor.MONO;
        }
        else if ( vendorName.toLowerCase().trim().equals( "dotgnu" ) )
        {
            return Vendor.DOTGNU;
        }
        else
        {
            throw new VendorUnsupportedException( "NAMVEN-100-000: Unknown vendor: Name = " + vendorName );
        }
    }
}
