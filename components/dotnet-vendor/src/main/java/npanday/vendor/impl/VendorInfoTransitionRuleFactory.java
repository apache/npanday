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

import npanday.InitializationException;
import npanday.PlatformUnsupportedException;
import npanday.model.settings.DefaultSetup;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.InvalidVersionFormatException;
import npanday.vendor.SettingsRepository;
import npanday.vendor.SettingsUtil;
import npanday.vendor.Vendor;
import npanday.vendor.VendorFactory;
import npanday.vendor.VendorInfo;
import npanday.vendor.VendorInfoRepository;
import npanday.vendor.VendorInfoTransitionRule;
import npanday.vendor.VendorRequirement;
import npanday.vendor.VendorRequirementState;
import org.codehaus.plexus.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides factory methods for creating vendor info transition rules. These rules usually can determine the
 * exact vendor info; but at times, it is a best guess.
 *
 * @author Shane Isbell
 * @see npanday.vendor.VendorRequirementState
 */
final class VendorInfoTransitionRuleFactory
{
    private RepositoryRegistry repositoryRegistry;

    private VendorInfoRepository vendorInfoRepository;

    private int defaultSettingsContentVersion = -1;

    /**
     * The default vendor as specified within the npanday-settings file
     */
    private Vendor cachedDefaultVendor;

    /**
     * The default vendor version as specified within the npanday-settings file
     */
    private String cachedDefaultVendorVersion;

    /**
     * The default framework version as specified within the npanday-settings file
     */
    private String caechedDefaultFrameworkVersion;

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
        this.repositoryRegistry = repositoryRegistry;
        this.vendorInfoRepository = vendorInfoRepository;
        this.logger = logger;
        this.versionMatcher = new VersionMatcher();

        if ( repositoryRegistry == null )
        {
            throw new InitializationException( "NPANDAY-103-000: Unable to find the repository registry" );
        }

        refreshDefaultsCache();
    }

    private void refreshDefaultsCache( )
    {
        SettingsRepository settingsRepository = SettingsUtil.findSettingsFromRegistry( repositoryRegistry );

        if (defaultSettingsContentVersion < settingsRepository.getContentVersion()){
            final DefaultSetup defaultSetup = settingsRepository.getDefaultSetup();
            if (defaultSetup != null){
                defaultSettingsContentVersion = settingsRepository.getContentVersion();
                cachedDefaultVendor = VendorFactory.createVendorFromName( defaultSetup.getVendorName() );
                cachedDefaultVendorVersion = defaultSetup.getVendorVersion().trim();
                caechedDefaultFrameworkVersion = defaultSetup.getFrameworkVersion().trim();
            }
        }
    }

    public Vendor getDefaultVendor()
    {
        refreshDefaultsCache();
        return cachedDefaultVendor;
    }

    public String getDefaultVendorVersion()
    {
        refreshDefaultsCache();
        return cachedDefaultVendorVersion;
    }

    public String getDefaultFrameworkVersion()
    {
        refreshDefaultsCache();
        return caechedDefaultFrameworkVersion;
    }

    VendorInfoTransitionRule createVendorInfoSetterForNTT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-003: Entering State = NTT" );
                return VendorRequirementState.EXIT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNFF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-004: Entering State = NFF" );
                logger.debug( "NPANDAY-103-041: Vendor:" + vendorInfo.getVendor() + ":default vendor:" + getDefaultVendor() );
                if ( vendorInfo.getVendor().equals( getDefaultVendor() ) )
                {
                    vendorInfo.setVendorVersion( getDefaultVendorVersion() );
                    logger.debug( "NPANDAY-103-042: Set default framework:" + getDefaultFrameworkVersion() );
                    vendorInfo.setFrameworkVersion( getDefaultFrameworkVersion() );
                    return VendorRequirementState.EXIT;
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
                                logger.warn( "NPANDAY-103-044: Hard code the framework (default framework:"
                                                 + getDefaultFrameworkVersion() + ")" );
                                vendorInfo.setFrameworkVersion( "2.0.50727" );
                                return VendorRequirementState.EXIT;
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
                                logger.warn( "NPANDAY-103-045: Hard code the framework (default framework:"
                                                 + getDefaultFrameworkVersion() + ")" );
                                vendorInfo.setFrameworkVersion(
                                    "2.0.50727" );  //TODO: this should be according to max version
                                return VendorRequirementState.EXIT;
                            }
                        }
                    }
                }
                return VendorRequirementState.EXIT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNFT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-007: Entering State = NFT" );
                if ( vendorInfo.getFrameworkVersion().equals( getDefaultFrameworkVersion() ) &&
                    vendorInfo.getVendor().equals( getDefaultVendor() ) )
                {
                    vendorInfo.setVendorVersion( getDefaultVendorVersion() );
                    return VendorRequirementState.NTT;
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
                                logger.error( "NPANDAY-103-039: Bad npanday-settings.xml file", e );
                                return VendorRequirementState.EXIT;
                            }
                            return VendorRequirementState.NTT;
                        }
                        else
                        {
                            return VendorRequirementState.EXIT;
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
                                logger.error( "NPANDAY-103-040: Bad npanday-settings.xml file", e );
                                return VendorRequirementState.EXIT;
                            }
                            return VendorRequirementState.NTT;
                        }
                        return VendorRequirementState.EXIT;
                    }
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForNTF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-009: Entering State = NTF" );
                logger.debug( "NPANDAY-103-049: Compare vendor version :" + getDefaultVendorVersion() + ":width:" + vendorInfo.getVendorVersion());
                
                if ( vendorInfo.getVendorVersion().equals( getDefaultVendorVersion() ) )
                {
                    logger.debug( "NPANDAY-103-049: Set to default framework:" + getDefaultFrameworkVersion() + ")" );
                    vendorInfo.setFrameworkVersion( getDefaultFrameworkVersion() );
                    vendorInfo.setVendor( getDefaultVendor() );
                    return VendorRequirementState.NTT;
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
                                logger.error( "NPANDAY-103-037: Bad npanday-settings.xml file", e );
                                return VendorRequirementState.EXIT;
                            }
                            return VendorRequirementState.NTT;
                        }
                        else
                        {
                            return VendorRequirementState.EXIT;
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
                                logger.error( "NPANDAY-103-038: Bad npanday-settings.xml file", e );
                                return VendorRequirementState.EXIT;
                            }
                            return VendorRequirementState.NTT;
                        }
                        else
                        {
                            return VendorRequirementState.EXIT;
                        }
                    }
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFTF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-011: Entering State = FTF" );
                logger.debug( "NPANDAY-103-067: Compare vendor version :" + getDefaultVendorVersion() + ":width:" + vendorInfo.getVendorVersion());

                if ( vendorInfo.getVendorVersion().equals( getDefaultVendorVersion() ) )
                {
                    logger.debug( "NPANDAY-103-065: Set to default version:" + getDefaultFrameworkVersion());

                    vendorInfo.setFrameworkVersion( getDefaultFrameworkVersion() );
                    vendorInfo.setVendor( getDefaultVendor() );
                    if ( getDefaultVendor().equals( Vendor.MICROSOFT ) )
                    {
                        return VendorRequirementState.MTT;
                    }
                    else if ( getDefaultVendor().equals( Vendor.MONO ) )
                    {
                        return VendorRequirementState.NTT;
                    }
                    else
                    {
                        return VendorRequirementState.GTT;
                    }
                }
                else
                {
                    List<VendorInfo> v = vendorInfoRepository.getVendorInfosFor( vendorInfo, true );
                    if ( !v.isEmpty() )
                    {
                        for ( VendorInfo vi : v )
                        {
                            logger.debug( "NPANDAY-103-064: Compare vendor version :" + vi.getVendorVersion() + ":width:" + vendorInfo.getVendorVersion());

                            if ( vi.getVendorVersion().equals( vendorInfo.getVendorVersion() ) )
                            {
                                logger.debug( "NPANDAY-103-063: Set framework version:" + vi.getFrameworkVersion());

                                vendorInfo.setFrameworkVersion( vi.getFrameworkVersion() );
                                vendorInfo.setVendor( vi.getVendor() );
                                if ( vi.getVendor().equals( Vendor.MICROSOFT ) )
                                {
                                    return VendorRequirementState.MTT;
                                }
                                else if ( vi.getVendor().equals( Vendor.MONO ) )
                                {
                                    return VendorRequirementState.NTT;
                                }
                                else
                                {
                                    return VendorRequirementState.GTT;
                                }
                            }
                        }
                        return VendorRequirementState.EXIT;
                    }
                    else
                    {
                        v = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                        for ( VendorInfo vi : v )
                        {
                            logger.debug( "NPANDAY-103-062: Compare vendor version :" + vi.getVendorVersion() + ":width:" + vendorInfo.getVendorVersion());
                        
                            if ( vi.getVendorVersion().equals( vendorInfo.getVendorVersion() ) )
                            {
                                logger.debug( "NPANDAY-103-061: Set framework version:" + vi.getFrameworkVersion());
                            
                                vendorInfo.setFrameworkVersion( vi.getFrameworkVersion() );
                                vendorInfo.setVendor( vi.getVendor() );
                                if ( vi.getVendor().equals( Vendor.MICROSOFT ) )
                                {
                                    return VendorRequirementState.MTT;
                                }
                                else if ( vi.getVendor().equals( Vendor.MONO ) )
                                {
                                    return VendorRequirementState.NTT;
                                }
                                else
                                {
                                    return VendorRequirementState.GTT;
                                }
                            }
                        }
                        return VendorRequirementState.EXIT;
                    }
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFFT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-012: Entering State = FFT" );
                if ( vendorInfo.getFrameworkVersion().equals( getDefaultFrameworkVersion() ) )
                {
                    vendorInfo.setVendorVersion( getDefaultVendorVersion() );
                    vendorInfo.setVendor( getDefaultVendor() );
                    if ( getDefaultVendor().equals( Vendor.MICROSOFT ) )
                    {
                        return VendorRequirementState.MTT;
                    }
                    else if ( getDefaultVendor().equals( Vendor.MONO ) )
                    {
                        return VendorRequirementState.NTT;
                    }
                    else
                    {
                        return VendorRequirementState.GTT;
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
                        return VendorRequirementState.EXIT;
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
                                    return VendorRequirementState.MTT;
                                }
                                else if ( vi.getVendor().equals( Vendor.MONO ) )
                                {
                                    return VendorRequirementState.NTT;
                                }
                                else
                                {
                                    return VendorRequirementState.GTT;
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
                                return VendorRequirementState.MTT;
                            }
                            else if ( vi.getVendor().equals( Vendor.MONO ) )
                            {
                                return VendorRequirementState.NTT;
                            }
                            else
                            {
                                return VendorRequirementState.GTT;
                            }
                        }
                    }
                    return VendorRequirementState.EXIT;
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFTT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-015: Entering State = FTT" );
                List<VendorInfo> vendorInfos = vendorInfoRepository.getVendorInfosFor( vendorInfo, false );
                if ( vendorInfos.isEmpty() )
                {
                    return VendorRequirementState.EXIT;
                }
                Vendor vendor = vendorInfos.get( 0 ).getVendor();//TODO: Do default branch
                vendorInfo.setVendor( vendor );
                if ( vendor.equals( Vendor.MICROSOFT ) )
                {
                    return VendorRequirementState.MTT;
                }
                else if ( vendor.equals( Vendor.MONO ) )
                {
                    return VendorRequirementState.NTT;
                }
                else
                {
                    return VendorRequirementState.GTT;
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForFFF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-017: Entering State = FFF" );
                vendorInfo.setVendor( getDefaultVendor() );
                vendorInfo.setVendorVersion( getDefaultVendorVersion() );
                logger.debug( "NPANDAY-103-052: Set defaults: " + getDefaultFrameworkVersion() );
                vendorInfo.setFrameworkVersion( getDefaultFrameworkVersion() );
                return VendorRequirementState.EXIT;
            }
        };
    }


    VendorInfoTransitionRule createVendorInfoSetterForMTT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-018: Entering State = MTT" );
                return VendorRequirementState.EXIT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForMTF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-019: Entering State = MTF" );
                logger.debug( "NPANDAY-103-053: Set to framework version:" + vendorInfo.getVendorVersion());
                vendorInfo.setFrameworkVersion( vendorInfo.getVendorVersion() );
                return VendorRequirementState.MTT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForMFT()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-020: Entering State = MTF" );
                vendorInfo.setVendorVersion( vendorInfo.getFrameworkVersion() );
                return VendorRequirementState.MTT;
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForMFF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-022: Entering State = MFF" );
                if ( vendorInfo.getVendor().equals( getDefaultVendor() ) )
                {
                    vendorInfo.setVendorVersion( getDefaultVendorVersion() );
                    return VendorRequirementState.MTF;
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
                        return VendorRequirementState.MTF;
                    }
                    catch ( InvalidVersionFormatException e )
                    {
                        logger.error( "NPANDAY-103-030: Invalid version. Unable to determine best vendor version", e );
                        return VendorRequirementState.EXIT;
                    }
                }
            }
        };
    }

    VendorInfoTransitionRule createVendorInfoSetterForGFF()
    {
        return new VendorInfoTransitionRule()
        {
            public VendorRequirementState process( VendorRequirement vendorInfo )
            {
                logger.debug( "NPANDAY-103-035: Entering State = GFF" );
                if ( vendorInfo.getVendor().equals( getDefaultVendor() ) )
                {
                    vendorInfo.setVendorVersion( getDefaultVendorVersion() );
                    logger.warn(
                        "NPANDAY-103-059: Hardcode framework version (default:" + getDefaultFrameworkVersion() + ")" );
                    vendorInfo.setFrameworkVersion( "2.0.50727" );
                    return VendorRequirementState.EXIT;
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
                        logger.debug( "NPANDAY-103-060: Hardcode framework version (default:" + getDefaultFrameworkVersion() + ")" );
                        vendorInfo.setFrameworkVersion( "2.0.50727" );
                        return VendorRequirementState.EXIT;
                    }
                    catch ( InvalidVersionFormatException e )
                    {
                        logger.error( "NPANDAY-103-031: Invalid version. Unable to determine best vendor version", e );
                        return VendorRequirementState.EXIT;
                    }
                }
            }
        };
    }
}
