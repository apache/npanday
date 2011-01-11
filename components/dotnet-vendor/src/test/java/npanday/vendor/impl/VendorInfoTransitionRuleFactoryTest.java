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
package npanday.vendor.impl;

import junit.framework.TestCase;
import npanday.InitializationException;
import npanday.vendor.VendorInfoTransitionRule;
import npanday.vendor.VendorInfo;
import npanday.vendor.VendorInfoState;
import npanday.vendor.Vendor;
import npanday.vendor.VendorTestFactory;
import npanday.model.settings.DefaultSetup;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;

public class VendorInfoTransitionRuleFactoryTest
    extends TestCase
{
    public void testNTF()
    {
        List<VendorInfo> vendorInfoList = new ArrayList<VendorInfo>();
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "1.1.4322" ) );
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "2.0.50727" ) );

        VendorInfoTransitionRuleFactory factory = Factory.getVendorInfoTransitionRuleFactory(
            VendorTestFactory.getDefaultSetup( "MICROSOFT", "2.0.50727", "2.0.50727" ), vendorInfoList );

        VendorInfoTransitionRule rule = factory.createVendorInfoSetterForNTF();
        VendorInfo vendorInfo = VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", null );
        VendorInfoState vendorInfoState = rule.process( vendorInfo );
        assertEquals( "Incorrect Vendor State", vendorInfoState, VendorInfoState.NTT );
        assertEquals( "Incorrect Vendor", Vendor.MONO, vendorInfo.getVendor() );
        assertEquals( "2.0.50727", vendorInfo.getFrameworkVersion() );
        assertEquals( "1.1.18", vendorInfo.getVendorVersion() );
    }

    public void testNFF()
    {
        List<VendorInfo> vendorInfoList = new ArrayList<VendorInfo>();
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "1.1.4322" ) );
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "2.0.50727" ) );

        VendorInfoTransitionRuleFactory factory = Factory.getVendorInfoTransitionRuleFactory(
            VendorTestFactory.getDefaultSetup( "MICROSOFT", "2.0.50727", "2.0.50727" ), vendorInfoList );

        VendorInfoTransitionRule rule = factory.createVendorInfoSetterForNFF();
        VendorInfo vendorInfo = VendorTestFactory.getVendorInfo( Vendor.MONO, null, null );
        VendorInfoState vendorInfoState = rule.process( vendorInfo );
        assertEquals( "Incorrect Vendor State", vendorInfoState, VendorInfoState.POST_PROCESS );
        assertEquals( Vendor.MONO, vendorInfo.getVendor() );
        assertEquals( "2.0.50727", vendorInfo.getFrameworkVersion() );
        assertEquals( "1.1.18", vendorInfo.getVendorVersion() );
    }

    public void testNFF_MatchDefaultVendor()
    {
        List<VendorInfo> vendorInfoList = new ArrayList<VendorInfo>();
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "2.0.50727" ) );
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "1.1.4322" ) );

        VendorInfoTransitionRuleFactory factory = Factory.getVendorInfoTransitionRuleFactory(
            VendorTestFactory.getDefaultSetup( "MONO", "1.1.18", "1.1.4322" ), vendorInfoList );

        VendorInfoTransitionRule rule = factory.createVendorInfoSetterForNFF();
        VendorInfo vendorInfo = VendorTestFactory.getVendorInfo( Vendor.MONO, null, null );
        VendorInfoState vendorInfoState = rule.process( vendorInfo );
        assertEquals( "Incorrect Vendor State", vendorInfoState, VendorInfoState.POST_PROCESS );
        assertEquals( Vendor.MONO, vendorInfo.getVendor() );
        assertEquals( "1.1.4322", vendorInfo.getFrameworkVersion() );
        assertEquals( "1.1.18", vendorInfo.getVendorVersion() );
    }

    public void testNFT()
    {
        List<VendorInfo> vendorInfoList = new ArrayList<VendorInfo>();
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.13", "2.0.50727" ) );
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "2.0.50727" ) );

        VendorInfoTransitionRuleFactory factory = Factory.getVendorInfoTransitionRuleFactory(
            VendorTestFactory.getDefaultSetup( "MICROSOFT", "2.0.50727", "2.0.50727" ), vendorInfoList );

        VendorInfoTransitionRule rule = factory.createVendorInfoSetterForNFT();
        VendorInfo vendorInfo = VendorTestFactory.getVendorInfo( Vendor.MONO, null, "2.0.50727" );
        VendorInfoState vendorInfoState = rule.process( vendorInfo );
        assertEquals( "Incorrect Vendor State", vendorInfoState, VendorInfoState.NTT );
        assertEquals( Vendor.MONO, vendorInfo.getVendor() );
        assertEquals( "2.0.50727", vendorInfo.getFrameworkVersion() );
        assertEquals( "1.1.18", vendorInfo.getVendorVersion() );
    }

    public void testNFT_WithMatchingDefault()
    {
        List<VendorInfo> vendorInfoList = new ArrayList<VendorInfo>();
        vendorInfoList.add( VendorTestFactory.getVendorInfo( Vendor.MONO, "1.1.18", "2.0.50727" ) );

        VendorInfoTransitionRuleFactory factory = Factory.getVendorInfoTransitionRuleFactory(
            VendorTestFactory.getDefaultSetup( "MONO", "1.1.18", "2.0.50727" ), vendorInfoList );

        VendorInfoTransitionRule rule = factory.createVendorInfoSetterForNFT();
        VendorInfo vendorInfo = VendorTestFactory.getVendorInfo( Vendor.MONO, null, "2.0.50727" );
        VendorInfoState vendorInfoState = rule.process( vendorInfo );
        assertEquals( "Incorrect Vendor State", vendorInfoState, VendorInfoState.NTT );
        assertEquals( Vendor.MONO, vendorInfo.getVendor() );
        assertEquals( "2.0.50727", vendorInfo.getFrameworkVersion() );
        assertEquals( "1.1.18", vendorInfo.getVendorVersion() );
    }

    public void testNFT_CantFindMatchingVendorInfo()
    {
        VendorInfoTransitionRuleFactory factory = Factory.getVendorInfoTransitionRuleFactory(
            VendorTestFactory.getDefaultSetup( "MICROSOFT", "2.0.50727", "2.0.50727" ), new ArrayList<VendorInfo>() );

        VendorInfoTransitionRule rule = factory.createVendorInfoSetterForNFT();
        VendorInfo vendorInfo = VendorTestFactory.getVendorInfo( Vendor.MONO, null, "2.0.50727" );
        VendorInfoState vendorInfoState = rule.process( vendorInfo );
        assertEquals( "Incorrect Vendor State", vendorInfoState, VendorInfoState.POST_PROCESS );
    }

    private static class Factory
    {
        static VendorInfoTransitionRuleFactory getVendorInfoTransitionRuleFactory( DefaultSetup defaultSetup,
                                                                                   List<VendorInfo> vendorInfos )
        {
            SettingsRepository settingsRepository = new SettingsRepository();
            try
            {
                Field field = settingsRepository.getClass().getDeclaredField( "defaultSetup" );
                field.setAccessible( true );
                field.set( settingsRepository, defaultSetup );
            }
            catch ( NoSuchFieldException e )
            {
                e.printStackTrace();
            }
            catch ( IllegalAccessException e )
            {
                e.printStackTrace();
            }
            RepositoryRegistryTestStub repositoryRegistry = new RepositoryRegistryTestStub();
            repositoryRegistry.setSettingRepository( settingsRepository );

            VendorInfoRepositoryTestStub vendorInfoRepository = new VendorInfoRepositoryTestStub();
            vendorInfoRepository.setVendorInfos( vendorInfos );

            VendorInfoTransitionRuleFactory factory = new VendorInfoTransitionRuleFactory();
            try
            {
                factory.init( repositoryRegistry, vendorInfoRepository,
                              new ConsoleLogger( Logger.LEVEL_INFO, "test" ) );
            }
            catch ( InitializationException e )
            {
                e.printStackTrace();
            }
            return factory;
        }
    }
}
