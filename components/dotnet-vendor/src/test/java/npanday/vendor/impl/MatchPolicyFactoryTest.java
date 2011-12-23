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

import junit.framework.TestCase;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.logging.Logger;
import npanday.vendor.VendorInfoMatchPolicy;
import npanday.vendor.VendorInfo;
import npanday.vendor.VendorTestFactory;
import npanday.vendor.Vendor;

/**
 *
 * @author Shane Isbell
 */
public class MatchPolicyFactoryTest
    extends TestCase
{
    private MatchPolicyFactory matchPolicyFactory;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    public void setUp()
    {
        matchPolicyFactory = new MatchPolicyFactory();
        logger = new ConsoleLogger( Logger.LEVEL_WARN, "test" );
        matchPolicyFactory.init( logger );
    }

    public void testCreateVendorNamePolicy()
    {
        VendorInfoMatchPolicy matchPolicy = matchPolicyFactory.createVendorNamePolicy( "MICROSOFT" );
        assertTrue( matchPolicy.match( VendorTestFactory.getVendorInfo( Vendor.MICROSOFT, "", "" ) ) );
        assertFalse( matchPolicy.match( VendorTestFactory.getVendorInfo( Vendor.MONO, "", "" ) ) );
    }

    public void testCreateVendorVersionPolicy()
    {
        VendorInfoMatchPolicy matchPolicy = matchPolicyFactory.createVendorVersionPolicy( "1.1.18" );
        assertTrue( matchPolicy.match( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "" ) ) );
        assertFalse( matchPolicy.match( VendorTestFactory.getVendorInfo( Vendor.MONO, "2.1.18", "" ) ) );
    }

    public void testCreateFrameworkVersionPolicy()
    {
        VendorInfoMatchPolicy matchPolicy = matchPolicyFactory.createFrameworkVersionPolicy( "2.0.50727" );
        assertTrue( matchPolicy.match( VendorTestFactory.getVendorInfo( Vendor.MONO, "", "2.0.50727" ) ) );
        assertFalse( matchPolicy.match( VendorTestFactory.getVendorInfo( Vendor.MONO, "", "1.1.4322" ) ) );
    }

    public void testCreateFrameworkVersionPolicy_WithNullValue()
    {
        VendorInfoMatchPolicy matchPolicy = matchPolicyFactory.createFrameworkVersionPolicy( "2.0.50727" );
        assertFalse( matchPolicy.match( VendorTestFactory.getVendorInfo( Vendor.MONO, "", null ) ) );
    }

    public void testCreateIsDefaultPolicy_WithNullVendorInfo()
    {
        VendorInfoMatchPolicy matchPolicy = matchPolicyFactory.createVendorIsDefaultPolicy();
        assertFalse( matchPolicy.match( null ) );
    }

    public void testCreateIsDefaultPolicy_False()
    {
        VendorInfoMatchPolicy matchPolicy = matchPolicyFactory.createVendorIsDefaultPolicy();
        VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
        vendorInfo.setDefault( false );
        assertFalse( matchPolicy.match( vendorInfo ) );
    }

    public void testCreateIsDefaultPolicy_True()
    {
        VendorInfoMatchPolicy matchPolicy = matchPolicyFactory.createVendorIsDefaultPolicy();
        VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
        vendorInfo.setDefault( true );
        assertTrue( matchPolicy.match( vendorInfo ) );
    }
}
