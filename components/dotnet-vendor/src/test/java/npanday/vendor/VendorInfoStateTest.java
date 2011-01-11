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

import junit.framework.TestCase;

public class VendorInfoStateTest
    extends TestCase
{

    public void testMTT()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.MICROSOFT, "2.0.50727", "2.0.50727" ) );
        assert(VendorInfoState.MTT.equals( vendorInfoState));
    }

    public void testMFT()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.MICROSOFT, null, "2.0.50727" ) );
        assert(VendorInfoState.MFT.equals( vendorInfoState));
    }

    public void testMFF()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.MICROSOFT, null, null ) );
        assert(VendorInfoState.MFF.equals( vendorInfoState));
    }

    public void testMTF()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.MICROSOFT, "2.0.50727", null ) );
        assert(VendorInfoState.MTF.equals( vendorInfoState));
    }

    public void testNTT()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "2.0.50727" ) );
        assert(VendorInfoState.NTT.equals( vendorInfoState));
    }

    public void testNFT()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.MONO, null, "2.0.50727" ) );
        assert(VendorInfoState.NFT.equals( vendorInfoState));
    }

    public void testNFF()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.MONO, null, null ) );
        assert(VendorInfoState.NFF.equals( vendorInfoState));
    }

    public void testNTF()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", null ) );
        assert(VendorInfoState.NTF.equals( vendorInfoState));
    }

    public void testGTT()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.DOTGNU, "0.7.2", "2.0.50727" ) );
        assert(VendorInfoState.GTT.equals( vendorInfoState));
    }

    public void testGFT()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.DOTGNU, null, "2.0.50727" ) );
        assert(VendorInfoState.GFT.equals( vendorInfoState));
    }

    public void testGFF()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.DOTGNU, null, null ) );
        assert(VendorInfoState.GFF.equals( vendorInfoState));
    }

    public void testGTF()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.DOTGNU, "0.7.2", null ) );
        assert(VendorInfoState.GTF.equals( vendorInfoState));
    }

    public void testFTT()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( null, "0.7.2", "2.0.50727" ) );
        assert(VendorInfoState.FTT.equals( vendorInfoState));
    }

    public void testFFT()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo(null, null, "2.0.50727" ) );
        assert(VendorInfoState.FFT.equals( vendorInfoState));
    }

    public void testFFF()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( null, null, null ) );
        assert(VendorInfoState.FFF.equals( vendorInfoState));
    }

    public void testFTF()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( null, "0.7.2", null ) );
        assert(VendorInfoState.FTF.equals( vendorInfoState));
    }

    public void testGFT_WithEmptyStrings()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.DOTGNU, "", "2.0.50727" ) );
        assert(VendorInfoState.GFT.equals( vendorInfoState));
    }

    public void testGFF_WithEmptyStrings()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.DOTGNU, "", "" ) );
        assert(VendorInfoState.GFF.equals( vendorInfoState));
    }

    public void testGTF_WithEmptyStrings()
    {
        VendorInfoState vendorInfoState =
            VendorInfoState.NULL.getState(VendorTestFactory.getVendorInfo( Vendor.DOTGNU, "0.7.2", "" ) );
        assert(VendorInfoState.GTF.equals( vendorInfoState));
    }
}
