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

public class VendorRequirementStateTest
    extends TestCase
{

    public void testMTT()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.MICROSOFT, "2.0.50727", "2.0.50727" ) );
        assert ( VendorRequirementState.MTT.equals( vendorRequirementState ) );
    }

    public void testMFT()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.MICROSOFT, null, "2.0.50727" ) );
        assert ( VendorRequirementState.MFT.equals( vendorRequirementState ) );
    }

    public void testMFF()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.MICROSOFT, null, null ) );
        assert ( VendorRequirementState.MFF.equals( vendorRequirementState ) );
    }

    public void testMTF()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.MICROSOFT, "2.0.50727", null ) );
        assert ( VendorRequirementState.MTF.equals( vendorRequirementState ) );
    }

    public void testNTT()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.MONO, "1.1.18", "2.0.50727" ) );
        assert ( VendorRequirementState.NTT.equals( vendorRequirementState ) );
    }

    public void testNFT()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.MONO, null, "2.0.50727" ) );
        assert ( VendorRequirementState.NFT.equals( vendorRequirementState ) );
    }

    public void testNFF()
    {
        VendorRequirementState vendorRequirementState =
            VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.MONO, null, null ) );
        assert ( VendorRequirementState.NFF.equals( vendorRequirementState ) );
    }

    public void testNTF()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.MONO, "1.1.18", null ) );
        assert ( VendorRequirementState.NTF.equals( vendorRequirementState ) );
    }

    public void testGTT()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.DOTGNU, "0.7.2", "2.0.50727" ) );
        assert ( VendorRequirementState.GTT.equals( vendorRequirementState ) );
    }

    public void testGFT()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.DOTGNU, null, "2.0.50727" ) );
        assert ( VendorRequirementState.GFT.equals( vendorRequirementState ) );
    }

    public void testGFF()
    {
        VendorRequirementState vendorRequirementState =
            VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.DOTGNU, null, null ) );
        assert ( VendorRequirementState.GFF.equals( vendorRequirementState ) );
    }

    public void testGTF()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( Vendor.DOTGNU, "0.7.2", null ) );
        assert ( VendorRequirementState.GTF.equals( vendorRequirementState ) );
    }

    public void testFTT()
    {
        VendorRequirementState vendorRequirementState = VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( null, "0.7.2", "2.0.50727" ) );
        assert ( VendorRequirementState.FTT.equals( vendorRequirementState ) );
    }

    public void testFFT()
    {
        VendorRequirementState vendorRequirementState =
            VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( null, null, "2.0.50727" ) );
        assert ( VendorRequirementState.FFT.equals( vendorRequirementState ) );
    }

    public void testFFF()
    {
        VendorRequirementState vendorRequirementState =
            VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( null, null, null ) );
        assert ( VendorRequirementState.FFF.equals( vendorRequirementState ) );
    }

    public void testFTF()
    {
        VendorRequirementState vendorRequirementState =
            VendorRequirementState.NULL.getState( VendorTestFactory.getVendorRequirement( null, "0.7.2", null ) );
        assert ( VendorRequirementState.FTF.equals( vendorRequirementState ) );
    }
}
