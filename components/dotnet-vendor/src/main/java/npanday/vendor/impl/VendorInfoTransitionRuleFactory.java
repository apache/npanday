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

import npanday.vendor.*;
import npanday.InitializationException;
import npanday.PlatformUnsupportedException;
import npanday.registry.RepositoryRegistry;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.File;

import org.codehaus.plexus.logging.Logger;

/**
 * Provides factory methods for creating vendor info transition rules. These rules usually can determine the
 * exact vendor info; but at times, it is a best guess.
 *
 * @author Shane Isbell
 * @see VendorInfoState
 */
final class VendorInfoTransitionRuleFactory
{

    private VendorInfoRepository vendorInfoRepository;

    /**
     * The default vendor as specified within the npanday-settings file
     */
    private Vendor defaultVendor;

    /**
     * The default vendor version as specified within the npanday-settings file
     */
    private String defaultVendorVersion;

    /**
     * The default framework version as specified within the npanday-settings file
     */
    private String defaultFrameworkVersion;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * A version matcher
     */
    VersionMatcher versionMatcher;

    /**
     * Default constructor
     */
    VendorInfoTransitionRuleFactory()
    {
    }

    /**
     * Initializes this factory.
     *
     * @param repositoryRegistry   the repository registry containing various NPanday config information.
     * @param vendorInfoRepository the vendor info repository used for accessing the npanday-settings config file
     * @param logger               the plexus logger
     * @throws InitializationException if there is a problem initializing this factory
     */
    void init( RepositoryRegistry repositoryRegistry, VendorInfoRepository vendorInfoRepository, Logger logger )
        throws InitializationException
    {
        this.vendorInfoRepository = vendorInfoRepository;
        this.logger = logger;
        if ( repositoryRegistry == null )
        {
            throw new InitializationException( "NPANDAY-103-000: Unable to find the repository registry" );
        }
        logger.debug( "NPANDAY-103-036.0: Respository registry: " + repositoryRegistry);
        
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
        if ( settingsRepository == null )
        {
            throw new InitializationException(
                "NPANDAY-103-001: Settings Repository is null. Aborting initialization of VendorInfoTranstionRuleFactory" );
        }

        try
        {
            defaultVendor = VendorFactory.createVendorFromName( settingsRepository.getDefaultSetup().getVendorName() );
            logger.debug( "NPANDAY-103-036: Default Vendor Initialized: Name = " + defaultVendor );
        }
        catch ( VendorUnsupportedException e )
        {
            throw new InitializationException( "NPANDAY-103-002: Unknown Default Vendor: Name = " + defaultVendor );
        }
        defaultVendorVersion = settingsRepository.getDefaultSetup().getVendorVersion().trim();
        defaultFrameworkVersion = settingsRepository.getDefaultSetup().getFrameworkVersion().trim();
        this.versionMatcher = new VersionMatcher();
    }

    VendorInfoTransitionRule createPostProcessRule()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-034: Entering State = Post Process, applying executable paths" );
                if ( ( vendorInfo.getExecutablePaths() == null || vendorInfo.getExecutablePaths().size() == 0 ) &&
                    vendorInfoRepository.exists() )
                {
                    try
                    {
                        List<File> existingPaths = new ArrayList<File>();
                        List<File> configuredExecutablePaths = vendorInfoRepository.getConfiguredVendorInfoByExample(vendorInfo).getExecutablePaths();
                        for(File path : configuredExecutablePaths){
                            if (!path.exists()) {
                                logger.debug( "NPANDAY-103-61: Configured path does not exist and is therefore omitted: " + path );
                            }
                            else {
                                existingPaths.add(path);
                            }
                        }
                        vendorInfo.setExecutablePaths( existingPaths );
                    }
                    catch ( PlatformUnsupportedException e )
                    {
                        logger.debug( "NPANDAY-103-36: Failed to resolve configured executable paths." );
                    }
                }
                return VendorInfoState.EXIT;
            }
        };
    }

    /**
     * Returns the vendor info transition rule for state: Vendor is Novell, vendor version exists, framework version exists.
     *
     * @return the vendor info transition rule for state: Vendor is Novell, vendor version exists, framework version exists.
     */
    VendorInfoTransitionRule createVendorInfoSetterForNTT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-003: Entering State = NTT" );
                return VendorInfoState.POST_PROCESS;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNFF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-004: Entering State = NFF" );
                logger.debug( "NPANDAY-103-041: Vendor:" + vendorInfo.getVendor() + ":default vendor:" + defaultVendor );
                if ( vendorInfo.getVendor().equals( defaultVendor ) )
                {
                    vendorInfo.setVendorVersion( defaultVendorVersion );
                    logger.debug( "NPANDAY-103-042: Set default framework:" + defaultFrameworkVersion );
                    vendorInfo.setFrameworkVersion( defaultFrameworkVersion );
                    return VendorInfoState.POST_PROCESS;
                }
                else
                {
                    List<VendorInfo> v = vendorInfoRepository.getVendorInfosFor( vendorInfo, true );
                    if ( !v.isEmpty() )
                    {
                        for ( VendorInfo vi : v )
                        {
                            logger.debug( "NPANDAY-103-043: Compare vendor:" + vi.getVendor() + ":with vendor:" + vendorInfo.getVendor() );
                            if ( vi.getVendor().equals( vendorInfo.getVendor() ) )
                            {
                                vendorInfo.setVendorVersion( vi.getVendorVersion() );
                                logger.debug( "NPANDAY-103-044: Hard code the frameworkd (default framework:" + defaultFrameworkVersion + ")" );
                                vendorInfo.setFrameworkVersion( "2.0.50727" );
                                return VendorInfoState.POST_PROCESS;
                            }
                        }
                    }
                    else
                    {
                        v = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                        for ( VendorInfo vi : v )
                        {
                            logger.debug( "NPANDAY-103-046: Compare vendor:" + vi.getVendor() + ":with vendor:" + vendorInfo.getVendor() );
                            if ( vi.getVendor().equals( vendorInfo.getVendor() ) )
                            {
                                vendorInfo.setVendorVersion( vi.getVendorVersion() );
                                logger.debug( "NPANDAY-103-045: Hard code the frameworkd (default framework:" + defaultFrameworkVersion + ")" );
                                vendorInfo.setFrameworkVersion(
                                    "2.0.50727" );  //TODO: this should be according to max version
                                return VendorInfoState.POST_PROCESS;
                            }
                        }
                    }
                }
                return createVendorInfoSetterForNFF_NoSettings().process( vendorInfo );
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNFF_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-005: Entering State = NFF" );
                logger.debug( "NPANDAY-103-047: Hard code the frameworkd (default framework:" + defaultFrameworkVersion + ")" );
                vendorInfo.setFrameworkVersion( "2.0.50727" );
                return VendorInfoState.NFT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNFT_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-006: Entering State = NFT" );
                return VendorInfoState.POST_PROCESS; //NO WAY TO KNOW
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNFT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-007: Entering State = NFT" );
                if ( vendorInfo.getFrameworkVersion().equals( defaultFrameworkVersion ) &&
                    vendorInfo.getVendor().equals( defaultVendor ) )
                {
                    vendorInfo.setVendorVersion( defaultVendorVersion );
                    return VendorInfoState.NTT;
                }
                else
                {
                    List<VendorInfo> v = vendorInfoRepository.getVendorInfosFor( vendorInfo, true );
                    if ( !v.isEmpty() )
                    {
                        Set<String> vendorVersions = new HashSet<String>();
                        for ( VendorInfo vi : v )
                        {
                            if ( vi.getFrameworkVersion().equals( vendorInfo.getFrameworkVersion() ) )
                            {
                                vendorVersions.add( vi.getVendorVersion() );
                            }
                        }

                        if ( vendorVersions.size() > 0 )
                        {
                            try
                            {
                                vendorInfo.setVendorVersion( vendorInfoRepository.getMaxVersion( vendorVersions ) );
                            }
                            catch ( InvalidVersionFormatException e )
                            {
                                logger.warn( "NPANDAY-103-039: Bad npanday-settings.xml file", e );
                                return createVendorInfoSetterForNFT_NoSettings().process( vendorInfo );
                            }
                            return VendorInfoState.NTT;
                        }
                        else
                        {
                            return createVendorInfoSetterForNFT_NoSettings().process( vendorInfo );
                        }
                    }
                    else
                    {
                        v = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                        Set<String> vendorVersions = new HashSet<String>();
                        for ( VendorInfo vi : v )
                        {
                            if ( vi.getFrameworkVersion().equals( vendorInfo.getFrameworkVersion() ) )
                            {
                                vendorVersions.add( vi.getVendorVersion() );
                            }
                        }

                        if ( vendorVersions.size() > 0 )
                        {
                            try
                            {
                                vendorInfo.setVendorVersion( vendorInfoRepository.getMaxVersion( vendorVersions ) );
                            }
                            catch ( InvalidVersionFormatException e )
                            {
                                logger.warn( "NPANDAY-103-040: Bad npanday-settings.xml file", e );
                                return createVendorInfoSetterForNFT_NoSettings().process( vendorInfo );
                            }
                            return VendorInfoState.NTT;
                        }
                        return createVendorInfoSetterForNFT_NoSettings().process( vendorInfo );
                    }
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNTF_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-008: Entering State = NTF" );
                vendorInfo.setFrameworkVersion( "2.0.50727" );
                return VendorInfoState.NTT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNTF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-009: Entering State = NTF" );
                logger.debug( "NPANDAY-103-049: Compare vendor version :" + defaultVendorVersion + ":width:" + vendorInfo.getVendorVersion());
                
                if ( vendorInfo.getVendorVersion().equals( defaultVendorVersion ) )
                {
                    logger.debug( "NPANDAY-103-049: Set to default framework:" + defaultFrameworkVersion + ")" );
                    vendorInfo.setFrameworkVersion( defaultFrameworkVersion );
                    vendorInfo.setVendor( defaultVendor );
                    return VendorInfoState.NTT;
                }
                else
                {
                    List<VendorInfo> v = vendorInfoRepository.getVendorInfosFor( vendorInfo, true );
                    if ( !v.isEmpty() )
                    {
                        Set<String> frameworkVersions = new HashSet<String>();
                        for ( VendorInfo vi : v )
                        {
                            if ( vi.getVendorVersion().equals( vendorInfo.getVendorVersion() ) )
                            {
                                frameworkVersions.add( vi.getFrameworkVersion() );
                            }
                        }

                        if ( frameworkVersions.size() > 0 )
                        {
                            try
                            {
                                vendorInfo.setFrameworkVersion(
                                    vendorInfoRepository.getMaxVersion( frameworkVersions ) );
                            }
                            catch ( InvalidVersionFormatException e )
                            {
                                logger.warn( "NPANDAY-103-037: Bad npanday-settings.xml file", e );
                                return createVendorInfoSetterForNTF_NoSettings().process( vendorInfo );
                            }
                            return VendorInfoState.NTT;
                        }
                        else
                        {
                            return createVendorInfoSetterForNTF_NoSettings().process( vendorInfo );
                        }
                    }
                    else
                    {
                        v = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                        Set<String> frameworkVersions = new HashSet<String>();
                        for ( VendorInfo vi : v )
                        {
                            if ( vi.getVendorVersion().equals( vendorInfo.getVendorVersion() ) )
                            {
                                frameworkVersions.add( vi.getFrameworkVersion() );
                            }
                        }

                        if ( frameworkVersions.size() > 0 )
                        {
                            try
                            {
                                vendorInfo.setFrameworkVersion(
                                    vendorInfoRepository.getMaxVersion( frameworkVersions ) );
                            }
                            catch ( InvalidVersionFormatException e )
                            {
                                logger.warn( "NPANDAY-103-038: Bad npanday-settings.xml file", e );
                                return createVendorInfoSetterForNTF_NoSettings().process( vendorInfo );
                            }
                            return VendorInfoState.NTT;
                        }
                        else
                        {
                            return createVendorInfoSetterForNTF_NoSettings().process( vendorInfo );
                        }
                    }
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFTF_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-010: Entering State = FTF" );
                String vendorVersion = vendorInfo.getVendorVersion();
                if ( vendorVersion.equals( "2.0.50727" ) || vendorVersion.equals( "1.1.4322" ) )
                {
                    vendorInfo.setVendor( Vendor.MICROSOFT );
                    return VendorInfoState.MTF;
                }
                else
                {
                    vendorInfo.setVendor( Vendor.MONO );//This could be dotGNU: this is best guess
                    return VendorInfoState.NTF;
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFTF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-011: Entering State = FTF" );
                logger.debug( "NPANDAY-103-050: Compare vendor version :" + defaultVendorVersion + ":width:" + vendorInfo.getVendorVersion());

                if ( vendorInfo.getVendorVersion().equals( defaultVendorVersion ) )
                {
                    logger.debug( "NPANDAY-103-050: Set to default version:" + defaultFrameworkVersion);

                    vendorInfo.setFrameworkVersion( defaultFrameworkVersion );
                    vendorInfo.setVendor( defaultVendor );
                    if ( defaultVendor.equals( Vendor.MICROSOFT ) )
                    {
                        return VendorInfoState.MTT;
                    }
                    else if ( defaultVendor.equals( Vendor.MONO ) )
                    {
                        return VendorInfoState.NTT;
                    }
                    else
                    {
                        return VendorInfoState.GTT;
                    }
                }
                else
                {
                    List<VendorInfo> v = vendorInfoRepository.getVendorInfosFor( vendorInfo, true );
                    if ( !v.isEmpty() )
                    {
                        for ( VendorInfo vi : v )
                        {
                            logger.debug( "NPANDAY-103-050: Compare vendor version :" + vi.getVendorVersion() + ":width:" + vendorInfo.getVendorVersion());

                            if ( vi.getVendorVersion().equals( vendorInfo.getVendorVersion() ) )
                            {
                                logger.debug( "NPANDAY-103-050: Set framework version:" + vi.getFrameworkVersion());

                                vendorInfo.setFrameworkVersion( vi.getFrameworkVersion() );
                                vendorInfo.setVendor( vi.getVendor() );
                                if ( vi.getVendor().equals( Vendor.MICROSOFT ) )
                                {
                                    return VendorInfoState.MTT;
                                }
                                else if ( vi.getVendor().equals( Vendor.MONO ) )
                                {
                                    return VendorInfoState.NTT;
                                }
                                else
                                {
                                    return VendorInfoState.GTT;
                                }
                            }
                        }
                        return createVendorInfoSetterForFTF_NoSettings().process( vendorInfo );
                    }
                    else
                    {
                        v = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                        for ( VendorInfo vi : v )
                        {
                            logger.debug( "NPANDAY-103-050: Compare vendor version :" + vi.getVendorVersion() + ":width:" + vendorInfo.getVendorVersion());
                        
                            if ( vi.getVendorVersion().equals( vendorInfo.getVendorVersion() ) )
                            {
                                logger.debug( "NPANDAY-103-050: Set framework version:" + vi.getFrameworkVersion());
                            
                                vendorInfo.setFrameworkVersion( vi.getFrameworkVersion() );
                                vendorInfo.setVendor( vi.getVendor() );
                                if ( vi.getVendor().equals( Vendor.MICROSOFT ) )
                                {
                                    return VendorInfoState.MTT;
                                }
                                else if ( vi.getVendor().equals( Vendor.MONO ) )
                                {
                                    return VendorInfoState.NTT;
                                }
                                else
                                {
                                    return VendorInfoState.GTT;
                                }
                            }
                        }
                        return createVendorInfoSetterForFTF_NoSettings().process( vendorInfo );
                    }
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFFT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-012: Entering State = FFT" );
                if ( vendorInfo.getFrameworkVersion().equals( defaultFrameworkVersion ) )
                {
                    vendorInfo.setVendorVersion( defaultVendorVersion );
                    vendorInfo.setVendor( defaultVendor );
                    if ( defaultVendor.equals( Vendor.MICROSOFT ) )
                    {
                        return VendorInfoState.MTT;
                    }
                    else if ( defaultVendor.equals( Vendor.MONO ) )
                    {
                        return VendorInfoState.NTT;
                    }
                    else
                    {
                        return VendorInfoState.GTT;
                    }
                }
                else
                {
                    try
                    {
                        vendorInfo.setVendor( VendorFactory.getDefaultVendorForOS() );
                    }
                    catch ( PlatformUnsupportedException e )
                    {
                        return VendorInfoState.POST_PROCESS;
                    }
                    List<VendorInfo> v = vendorInfoRepository.getVendorInfosFor( vendorInfo, true );
                    if ( !v.isEmpty() )
                    {
                        for ( VendorInfo vi : v )
                        {
                            if ( vi.getFrameworkVersion().equals( vendorInfo.getFrameworkVersion() ) )
                            {
                                vendorInfo.setVendorVersion( vi.getVendorVersion() );
                                if ( vi.getVendor().equals( Vendor.MICROSOFT ) )
                                {
                                    return VendorInfoState.MTT;
                                }
                                else if ( vi.getVendor().equals( Vendor.MONO ) )
                                {
                                    return VendorInfoState.NTT;
                                }
                                else
                                {
                                    return VendorInfoState.GTT;
                                }
                            }
                        }
                    }
                    v = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                    for ( VendorInfo vi : v )
                    {
                        if ( vi.getFrameworkVersion().equals( vendorInfo.getFrameworkVersion() ) )
                        {
                            vendorInfo.setVendorVersion( vi.getVendorVersion() );
                            if ( vi.getVendor().equals( Vendor.MICROSOFT ) )
                            {
                                return VendorInfoState.MTT;
                            }
                            else if ( vi.getVendor().equals( Vendor.MONO ) )
                            {
                                return VendorInfoState.NTT;
                            }
                            else
                            {
                                return VendorInfoState.GTT;
                            }
                        }
                    }
                    return createVendorInfoSetterForFFT_NoSettings().process( vendorInfo );
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFFT_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-013: Entering State = FFT" );
                try
                {
                    vendorInfo.setVendor( VendorFactory.getDefaultVendorForOS() );
                }
                catch ( PlatformUnsupportedException e )
                {
                    return VendorInfoState.POST_PROCESS;
                }
                return ( vendorInfo.getVendor().equals( Vendor.MICROSOFT ) ) ? VendorInfoState.MFT
                    : VendorInfoState.NFT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFTT_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-014: Entering State = FTT" );
                String vendorVersion = vendorInfo.getVendorVersion();
                Vendor defaultVendor;
                try
                {
                    defaultVendor = VendorFactory.getDefaultVendorForOS();
                }
                catch ( PlatformUnsupportedException e )
                {
                    return VendorInfoState.POST_PROCESS;
                }
                if ( ( vendorVersion.equals( "2.0.50727" ) || vendorVersion.equals( "1.1.4322" ) ) &&
                    defaultVendor.equals( Vendor.MICROSOFT ) )
                {
                    vendorInfo.setVendor( Vendor.MICROSOFT );
                    return VendorInfoState.MTT;
                }
                else
                {
                    vendorInfo.setVendor( Vendor.MONO );//This could be dotGNU: this is best guess
                    return VendorInfoState.NTT;
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFTT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-015: Entering State = FTT" );
                List<VendorInfo> vendorInfos = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                if ( vendorInfos.isEmpty() )
                {
                    return createVendorInfoSetterForFTT_NoSettings().process( vendorInfo );
                }
                Vendor vendor = vendorInfos.get( 0 ).getVendor();//TODO: Do default branch
                vendorInfo.setVendor( vendor );
                if ( vendor.equals( Vendor.MICROSOFT ) )
                {
                    return VendorInfoState.MTT;
                }
                else if ( vendor.equals( Vendor.MONO ) )
                {
                    return VendorInfoState.NTT;
                }
                else
                {
                    return VendorInfoState.GTT;
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFFF_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-016: Entering State = FFF" );
                try
                {
                    vendorInfo.setVendor( VendorFactory.getDefaultVendorForOS() );
                }
                catch ( PlatformUnsupportedException e )
                {
                    return VendorInfoState.POST_PROCESS;
                }
                return ( vendorInfo.getVendor().equals( Vendor.MICROSOFT ) ) ? VendorInfoState.MFF
                    : VendorInfoState.NFF;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFFF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-017: Entering State = FFF" );
                vendorInfo.setVendor( defaultVendor );
                vendorInfo.setVendorVersion( defaultVendorVersion );
                logger.debug( "NPANDAY-103-052: Set to default framework version:" + defaultFrameworkVersion);
                vendorInfo.setFrameworkVersion( defaultFrameworkVersion );
                return VendorInfoState.POST_PROCESS;
            }
        };
    }


    VendorInfoTransitionRule createVendorInfoSetterForMTT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-018: Entering State = MTT" );
                return VendorInfoState.POST_PROCESS;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForMTF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-019: Entering State = MTF" );
                logger.debug( "NPANDAY-103-053: Set to framework version:" + vendorInfo.getVendorVersion());
                vendorInfo.setFrameworkVersion( vendorInfo.getVendorVersion() );
                return VendorInfoState.MTT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForMFT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-020: Entering State = MTF" );
                vendorInfo.setVendorVersion( vendorInfo.getFrameworkVersion() );
                return VendorInfoState.MTT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForMFF_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-021: Entering State = MFF" );
                String systemRoot = System.getenv("SystemRoot");
                String systemDrive = System.getenv("SystemDrive");
                File v1 = new File( systemRoot, "\\Microsoft.NET\\Framework\\v1.1.4322" );
                File v2 = new File( systemRoot, "\\Microsoft.NET\\Framework\\v2.0.50727" );
                File v3 = new File( systemDrive, "\\Program Files\\Microsoft.NET\\SDK\\v1.1" );
                File v4 = new File( systemDrive, "\\Program Files\\Microsoft.NET\\SDK\\v2.0" );
                List<File> executablePaths = new ArrayList<File>();

                if ( v2.exists() )
                {
                    logger.debug( "NPANDAY-103-055: Hardcode framework version (default:" + defaultFrameworkVersion + ")");
                    vendorInfo.setFrameworkVersion( "2.0.50727" );
                    executablePaths.add( v2 );
                    if ( v4.exists() )
                    {
                        executablePaths.add( v4 );
                    }
                }
                else if ( v1.exists() )
                {
                    logger.debug( "NPANDAY-103-056: Hardcode framework version (default:" + defaultFrameworkVersion + ")");
                    vendorInfo.setFrameworkVersion( "1.1.4322" );
                    executablePaths.add( v1 );
                    if ( v3.exists() )
                    {
                        executablePaths.add( v3 );
                    }
                }
                else
                {
                    logger.debug( "NPANDAY-103-057: Hardcode framework version:");
                    vendorInfo.setFrameworkVersion( "2.0.50727" );
                }

                vendorInfo.setExecutablePaths( executablePaths );
                return VendorInfoState.MFT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForMFF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-022: Entering State = MFF" );
                if ( vendorInfo.getVendor().equals( defaultVendor ) )
                {
                    vendorInfo.setVendorVersion( defaultVendorVersion );
                    return VendorInfoState.MTF;
                }
                else
                {
                    List<VendorInfo> vendorInfos = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                    Set<String> versions = new HashSet<String>();
                    for ( VendorInfo vi : vendorInfos )
                    {
                        String frameworkVersion = vi.getFrameworkVersion();
                        String vendorVersion = vi.getVendorVersion();
                        if ( frameworkVersion != null )
                        {
                            versions.add( frameworkVersion );
                        }
                        if ( vendorVersion != null )
                        {
                            versions.add( vi.getVendorVersion() );
                        }
                    }
                    try
                    {
                        String maxVersion = vendorInfoRepository.getMaxVersion( versions );
                        vendorInfo.setVendorVersion( maxVersion );
                        return VendorInfoState.MTF;
                    }
                    catch ( InvalidVersionFormatException e )
                    {
                        logger.info( "NPANDAY-103-030: Invalid version. Unable to determine best vendor version", e );
                        return createVendorInfoSetterForMFF_NoSettings().process( vendorInfo );
                    }
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForGFF_NoSettings()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-023: Entering State = GFF" );
                logger.debug( "NPANDAY-103-058: Hardcode framework version:");
                vendorInfo.setFrameworkVersion( "2.0.50727" );
                vendorInfo.setVendorVersion( "2.0.50727" );
                return VendorInfoState.POST_PROCESS;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForGFF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorInfoState process( VendorInfo vendorInfo )
            {
                logger.debug( "NPANDAY-103-035: Entering State = GFF" );
                if ( vendorInfo.getVendor().equals( defaultVendor ) )
                {
                    vendorInfo.setVendorVersion( defaultVendorVersion );
                    logger.debug( "NPANDAY-103-059: Hardcode framework version (default:" + defaultFrameworkVersion + ")" );
                    vendorInfo.setFrameworkVersion( "2.0.50727" );
                    return VendorInfoState.POST_PROCESS;
                }
                else
                {
                    List<VendorInfo> vendorInfos = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                    Set<String> versions = new HashSet<String>();
                    for ( VendorInfo vi : vendorInfos )
                    {
                        String vendorVersion = vi.getVendorVersion();
                        if ( vendorVersion != null )
                        {
                            versions.add( vi.getVendorVersion() );
                        }
                    }
                    try
                    {
                        String maxVersion = vendorInfoRepository.getMaxVersion( versions );
                        vendorInfo.setVendorVersion( maxVersion );
                        logger.debug( "NPANDAY-103-060: Hardcode framework version (default:" + defaultFrameworkVersion + ")" );
                        vendorInfo.setFrameworkVersion( "2.0.50727" );
                        return VendorInfoState.POST_PROCESS;
                    }
                    catch ( InvalidVersionFormatException e )
                    {
                        logger.info( "NPANDAY-103-031: Invalid version. Unable to determine best vendor version", e );
                        return createVendorInfoSetterForGFF_NoSettings().process( vendorInfo );
                    }
                }
            }
        };
    }
    //TODO: add additional DotGNU states
}
