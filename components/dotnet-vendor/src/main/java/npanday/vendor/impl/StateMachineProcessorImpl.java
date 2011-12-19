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

import npanday.PlatformUnsupportedException;
import npanday.model.settings.Framework;
import npanday.model.settings.Vendor;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.IllegalStateException;
import npanday.vendor.SettingsUtil;
import npanday.vendor.StateMachineProcessor;
import npanday.vendor.VendorFactory;
import npanday.vendor.VendorInfo;
import npanday.vendor.VendorInfoRepository;
import npanday.vendor.VendorInfoTransitionRule;
import npanday.vendor.VendorRequirement;
import npanday.vendor.VendorRequirementState;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides an implementation of the <code>StateMachineProcessor</code>.
 *
 * @author Shane Isbell
 */
public final class StateMachineProcessorImpl
    implements StateMachineProcessor, LogEnabled, Initializable
{

    /**
     * A registry component of repository (config) files
     */
    private RepositoryRegistry repositoryRegistry;

    private VendorInfoRepository vendorInfoRepository;

    private Map<VendorRequirementState, VendorInfoTransitionRule> transitionRules;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * Constructor. This method is intended to be invoked by the plexus-container, not by the application developer.
     */
    public StateMachineProcessorImpl()
    {
    }

    /**
     * @see LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    /**
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
        throws InitializationException
    {
        VendorInfoTransitionRuleFactory factory = new VendorInfoTransitionRuleFactory();

        try
        {
            factory.init( repositoryRegistry, vendorInfoRepository, logger );
        }
        catch ( npanday.InitializationException e )
        {
            throw new InitializationException( "NPANDAY-102-008: Initializing rule factory failed.", e );
        }

        transitionRules = new HashMap<VendorRequirementState, VendorInfoTransitionRule>();
        transitionRules.put( VendorRequirementState.MTT, factory.createVendorInfoSetterForMTT() );
        transitionRules.put( VendorRequirementState.MTF, factory.createVendorInfoSetterForMTF() );
        transitionRules.put( VendorRequirementState.MFT, factory.createVendorInfoSetterForMFT() );
        transitionRules.put( VendorRequirementState.NTT, factory.createVendorInfoSetterForNTT() );

        transitionRules.put( VendorRequirementState.MFF, factory.createVendorInfoSetterForMFF() );
        transitionRules.put( VendorRequirementState.FTF, factory.createVendorInfoSetterForFTF() );
        transitionRules.put( VendorRequirementState.FFT, factory.createVendorInfoSetterForFFT() );
        transitionRules.put( VendorRequirementState.FTT, factory.createVendorInfoSetterForFTT() );
        transitionRules.put( VendorRequirementState.FFF, factory.createVendorInfoSetterForFFF() );
        transitionRules.put( VendorRequirementState.NFT, factory.createVendorInfoSetterForNFT() );
        transitionRules.put( VendorRequirementState.NTF, factory.createVendorInfoSetterForNTF() );
        transitionRules.put( VendorRequirementState.NFF, factory.createVendorInfoSetterForNTT() );
        transitionRules.put( VendorRequirementState.NFF, factory.createVendorInfoSetterForNFF() );
        transitionRules.put( VendorRequirementState.GFF, factory.createVendorInfoSetterForGFF() );
    }

    /**
     * @see StateMachineProcessor#process(npanday.vendor.VendorRequirement)
     */
    public VendorInfo process( VendorRequirement vendorRequirement )
        throws IllegalStateException, PlatformUnsupportedException
    {
        SettingsUtil.warnIfSettingsAreEmpty( logger, repositoryRegistry );

        if ( !vendorRequirement.isComplete() )
        {
            VendorRequirementState startState = VendorRequirementState.START.getState( vendorRequirement );
            VendorInfoTransitionRule rule = transitionRules.get( startState );
            if ( rule == null )
            {
                throw new IllegalStateException(
                    "NPANDAY-102-002: Could not find rule for state: State = " + startState.name() );
            }
            for ( VendorRequirementState state = VendorRequirementState.START;
                  !state.equals( VendorRequirementState.EXIT ); )
            {
                logger.debug( "NPANDAY-102-003: Apply rule:" + rule );
                state = rule.process( vendorRequirement );
                logger.debug( "NPANDAY-102-004: Vendor info requirement after rule:" + vendorRequirement );
                rule = transitionRules.get( state );
                if ( rule == null && !state.equals( VendorRequirementState.EXIT ) )
                {
                    throw new IllegalStateException(
                        "NPANDAY-102-005: Could not find rule for state: State = " + state.name() );
                }
            }
        }

        if ( !vendorRequirement.isComplete() )
        {
            // TODO: Remove this block, as soon as vendor discovery is moved to java code
            if ( vendorInfoRepository.isEmpty() )
            {
                Vendor configuredVendor = new Vendor();
                configuredVendor.setVendorName( VendorFactory.getDefaultVendorForOS().getVendorName() );
                configuredVendor.setVendorVersion( "2.0" );

                Framework configuredFramework = new Framework();
                configuredFramework.setFrameworkVersion(  "2.0.50727" );
                configuredFramework.setInstallRoot( null );

                VendorInfo vendorInfo = new SettingsBasedVendorInfo( configuredVendor, configuredFramework );

                logger.warn( "NPANDAY-102-006: Chose sensible default, because there are no settings available yet:"
                                 + vendorInfo );

                return vendorInfo;
            }

            throw new IllegalStateException( "NPANDAY-102-007: Vendor info requirement could not be completed: " + vendorRequirement );
        }

        return vendorInfoRepository.getSingleVendorInfoByRequirement( vendorRequirement );
    }
}
