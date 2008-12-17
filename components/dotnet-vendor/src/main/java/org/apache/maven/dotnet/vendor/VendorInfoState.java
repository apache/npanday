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

/**
 * Provides a way to know how complete a vendor info object is or more concisely its state of completion.
 *
 * @author Shane Isbell
 */
public enum VendorInfoState
{
    /**
     * State of VendorInfo object: Vendor is Microsoft, vendor version exists, framework version exists
     */
    MTT,

    /**
     * State of VendorInfo object: Vendor is Microsoft, vendor version exists, framework version does not exist
     */
    MTF,

    /**
     * State of VendorInfo object:  Vendor is Microsoft, vendor version does not exist, framework version exists
     */
    MFT,

    /**
     * State of VendorInfo object:  Vendor is Microsoft, vendor version does not exist, framework version does not exist
     */
    MFF,

    /**
     * State of VendorInfo object: Vendor is Novell, vendor version exists, framework version exists
     */
    NTT,

    /**
     * State of VendorInfo object: Vendor is Novell, vendor version exists, framework version does not exist
     */
    NTF,

    /**
     * State of VendorInfo object:  Vendor is Novell, vendor version does not exist, framework version exists
     */
    NFT,

    /**
     * State of VendorInfo object:  Vendor is Novell, vendor version does not exist, framework version does not exist
     */
    NFF,

    /**
     * State of VendorInfo object: Vendor is GNU, vendor version exists, framework version exists
     */
    GTT,

    /**
     * State of VendorInfo object: Vendor is GNU, vendor version exists, framework version does not exist
     */
    GTF,

    /**
     * State of VendorInfo object:  Vendor is GNU, vendor version does not exist, framework version exists
     */
    GFT,

    /**
     * State of VendorInfo object:  Vendor is GNU vendor version does not exist, framework version does not exist
     */
    GFF,

    /**
     * State of VendorInfo object: Vendor is unknown, vendor version exists, framework version exists
     */
    FTT,

    /**
     * State of VendorInfo object: Vendor is unknown, vendor version exists, framework version does not exist
     */
    FTF,

    /**
     * State of VendorInfo object:  Vendor is unknown, vendor version does not exist, framework version exists
     */
    FFT,

    /**
     * State of VendorInfo object:  Vendor is unknown, vendor version does not exist, framework version does not exist
     */
    FFF,

    /**
     * Exit state of VendorInfo object
     */
    EXIT,

    /**
     * Start state of VendorInfo object
     */
    START,

    /**
     * Null state of VendorInfo object
     */
    NULL,

    /**
     * Post processing state
     */
    POST_PROCESS;

    /**
     * Returns the completion state of the specified vendor info
     *
     * @param vendorInfo the vendor info to determine the state of completion
     * @return the state of the specified vendor info
     */
    public VendorInfoState getState( VendorInfo vendorInfo )
    {
        if ( vendorInfo == null )
        {
            return NULL;
        }

        if ( vendorInfo.getVendorVersion() != null && vendorInfo.getVendorVersion().trim().equals( "" ) )
        {
            vendorInfo.setVendorVersion( null );
        }

        if ( vendorInfo.getFrameworkVersion() != null && vendorInfo.getFrameworkVersion().trim().equals( "" ) )
        {
            vendorInfo.setFrameworkVersion( null );
        }

        if ( vendorInfo.getVendor() == null )
        {
            if ( vendorInfo.getVendorVersion() == null )
            {
                if ( vendorInfo.getFrameworkVersion() == null )
                {
                    return FFF;
                }
                else
                {
                    return FFT;
                }
            }
            else
            {
                if ( vendorInfo.getFrameworkVersion() == null )
                {
                    return FTF;
                }
                else
                {
                    return FTT;
                }
            }
        }
        else if ( vendorInfo.getVendor().equals( Vendor.MICROSOFT ) )
        {
            if ( vendorInfo.getVendorVersion() == null )
            {
                if ( vendorInfo.getFrameworkVersion() == null )
                {
                    return MFF;
                }
                else
                {
                    return MFT;
                }
            }
            else
            {
                if ( vendorInfo.getFrameworkVersion() == null )
                {
                    return MTF;
                }
                else
                {
                    return MTT;
                }
            }
        }
        else if ( vendorInfo.getVendor().equals( Vendor.MONO ) )
        {
            if ( vendorInfo.getVendorVersion() == null )
            {
                if ( vendorInfo.getFrameworkVersion() == null )
                {
                    return NFF;
                }
                else
                {
                    return NFT;
                }
            }
            else
            {
                if ( vendorInfo.getFrameworkVersion() == null )
                {
                    return NTF;
                }
                else
                {
                    return NTT;
                }
            }
        }
        else if ( vendorInfo.getVendor().equals( Vendor.DOTGNU ) )
        {
            if ( vendorInfo.getVendorVersion() == null )
            {
                if ( vendorInfo.getFrameworkVersion() == null )
                {
                    return GFF;
                }
                else
                {
                    return GFT;
                }
            }
            else
            {
                if ( vendorInfo.getFrameworkVersion() == null )
                {
                    return GTF;
                }
                else
                {
                    return GTT;
                }
            }
        }
        else
        {
            return EXIT;
        }
    }
}
