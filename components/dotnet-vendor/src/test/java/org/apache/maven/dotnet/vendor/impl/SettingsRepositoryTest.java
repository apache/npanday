package org.apache.maven.dotnet.vendor.impl;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.apache.maven.dotnet.model.settings.DefaultSetup;
import org.apache.maven.dotnet.model.settings.Framework;
import org.apache.maven.dotnet.model.settings.Vendor;
import org.apache.maven.dotnet.vendor.VendorTestFactory;
import org.apache.maven.dotnet.PlatformUnsupportedException;

public class SettingsRepositoryTest
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
            File installRoot = settingsRepository.getInstallRootFor(  "MICROSOFT", "2.0.50727", "2.0.50727");
            assertEquals( new File(System.getenv("SystemRoot") + "\\Microsoft.NET\\Framework\\v2.0.50727"), installRoot );
        }
        catch ( PlatformUnsupportedException e )
        {
            fail("Unsupported Platform: Message = " + e.getMessage());
        }
    }

    private static class Factory
    {
        static SettingsRepository createSettingsRepository( List<Vendor> vendors, DefaultSetup defaultSetup )
        {
            SettingsRepository settingsRepository = new SettingsRepository();
            try
            {
                Field defaultSetupField = settingsRepository.getClass().getDeclaredField( "defaultSetup" );
                defaultSetupField.setAccessible( true );
                defaultSetupField.set( settingsRepository, defaultSetup );

                Field vendorsField = settingsRepository.getClass().getDeclaredField( "vendors" );
                vendorsField.setAccessible( true );
                vendorsField.set( settingsRepository, vendors );
            }
            catch ( NoSuchFieldException e )
            {
                e.printStackTrace();
            }
            catch ( IllegalAccessException e )
            {
                e.printStackTrace();
            }
            return settingsRepository;
        }
    }
}
