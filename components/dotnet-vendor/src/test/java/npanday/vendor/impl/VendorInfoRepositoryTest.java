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
import npanday.PlatformUnsupportedException;
import npanday.model.settings.DefaultSetup;
import npanday.model.settings.Framework;
import npanday.model.settings.Vendor;
import npanday.registry.NPandayRepositoryException;
import npanday.vendor.SettingsRepository;
import npanday.vendor.VendorRequirement;
import npanday.vendor.VendorTestFactory;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class VendorInfoRepositoryTest
    extends TestCase
{
    public void testGetInstallRoot()
    {
        DefaultSetup defaultSetup = VendorTestFactory.getDefaultSetup( "MICROSOFT", "2.0.50727", "2.0.50727" );

        //Supported Types
        List<Vendor> vendors = new ArrayList<Vendor>();
        Vendor vendor = new Vendor();
        vendor.setVendorName("MICROSOFT");
        vendor.setVendorVersion("2.0.50727");

        Framework framework = new Framework();
        framework.setFrameworkVersion("2.0.50727");
        framework.setInstallRoot(System.getenv("SystemRoot") + "\\Microsoft.NET\\Framework\\v2.0.50727");
        framework.setSdkInstallRoot(System.getenv("SystemDrive") + "\\Program Files\\Microsoft.NET\\SDK\\v2.0");
        vendor.addFramework( framework );

        vendors.add( vendor );

        SettingsRepository settingsRepository = Factory.createSettingsRepository( vendors, defaultSetup );
        try
        {
            VendorInfoRepositoryImpl repo = new VendorInfoRepositoryImpl();
            RepositoryRegistryTestStub registry = new RepositoryRegistryTestStub();
            registry.setSettingRepository( settingsRepository );
            repo.setRepositoryRegistry( registry );

            File installRoot = repo.getSingleVendorInfoByRequirement(
                new VendorRequirement( npanday.vendor.Vendor.MICROSOFT, "2.0.50727",  "2.0.50727")).getInstallRoot();
            assertEquals( new File(System.getenv("SystemRoot") + "\\Microsoft.NET\\Framework\\v2.0.50727"), installRoot );
        }
        catch ( PlatformUnsupportedException e )
        {
            fail("Unsupported Platform: Message = " + e.getMessage());
        }
    }

    private static class Factory
    {
        static SettingsRepository createSettingsRepository( final List<Vendor> vendors, final DefaultSetup defaultSetup )
        {
            return new SettingsRepository(){

                public List<Vendor> getVendors()
                {
                    return vendors;
                }

                public DefaultSetup getDefaultSetup()
                {
                    return defaultSetup;
                }

                public boolean isEmpty()
                {
                    return false;
                }

                public int getContentVersion()
                {
                    return 0;
                }

                public void load( URL source )
                    throws NPandayRepositoryException
                {

                }

                public void clearAll()
                    throws OperationNotSupportedException
                {

                }

                public void reloadAll()
                    throws IOException, NPandayRepositoryException, OperationNotSupportedException
                {

                }

                public void setProperties( Hashtable props )
                {

                }
            };
        }
    }
}
