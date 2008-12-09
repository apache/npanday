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
package org.apache.maven.dotnet.vendor.impl;

import org.apache.maven.dotnet.vendor.*;
import org.apache.maven.dotnet.vendor.IllegalStateException;
import org.apache.maven.dotnet.registry.RepositoryRegistry;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

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

    private Map<VendorInfoState, VendorInfoTransitionRule> transitionRules;

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
        transitionRules = new HashMap<VendorInfoState, VendorInfoTransitionRule>();
        transitionRules.put( VendorInfoState.MTT, factory.createVendorInfoSetterForMTT() );
        transitionRules.put( VendorInfoState.MTF, factory.createVendorInfoSetterForMTF() );
        transitionRules.put( VendorInfoState.MFT, factory.createVendorInfoSetterForMFT() );
        transitionRules.put( VendorInfoState.NTT, factory.createVendorInfoSetterForNTT() );
        transitionRules.put( VendorInfoState.POST_PROCESS, factory.createPostProcessRule() );
        try
        {
            factory.init( repositoryRegistry, vendorInfoRepository, logger );
            logger.debug( "NPANDAY-102-000: Initialized rule factory." );
            transitionRules.put( VendorInfoState.MFF, factory.createVendorInfoSetterForMFF() );
            transitionRules.put( VendorInfoState.FTF, factory.createVendorInfoSetterForFTF() );
            transitionRules.put( VendorInfoState.FFT, factory.createVendorInfoSetterForFFT() );
            transitionRules.put( VendorInfoState.FTT, factory.createVendorInfoSetterForFTT() );
            transitionRules.put( VendorInfoState.FFF, factory.createVendorInfoSetterForFFF() );
            transitionRules.put( VendorInfoState.NFT, factory.createVendorInfoSetterForNFT() );
            transitionRules.put( VendorInfoState.NTF, factory.createVendorInfoSetterForNTF() );
            transitionRules.put( VendorInfoState.NFF, factory.createVendorInfoSetterForNTT() );
            transitionRules.put( VendorInfoState.NFF, factory.createVendorInfoSetterForNFF() );
            transitionRules.put( VendorInfoState.GFF, factory.createVendorInfoSetterForGFF() );
        }
        catch ( org.apache.maven.dotnet.InitializationException e )
        {
            logger.debug( "NPANDAY-102-001: Unable to initialize rule factory.", e );
            transitionRules.put( VendorInfoState.MFF, factory.createVendorInfoSetterForMFF_NoSettings() );
            transitionRules.put( VendorInfoState.NFT, factory.createVendorInfoSetterForNFT_NoSettings() );
            transitionRules.put( VendorInfoState.NTF, factory.createVendorInfoSetterForNTF_NoSettings() );
            transitionRules.put( VendorInfoState.FTF, factory.createVendorInfoSetterForFTF_NoSettings() );
            transitionRules.put( VendorInfoState.FFT, factory.createVendorInfoSetterForFFT_NoSettings() );
            transitionRules.put( VendorInfoState.FTT, factory.createVendorInfoSetterForFTT_NoSettings() );
            transitionRules.put( VendorInfoState.FFF, factory.createVendorInfoSetterForFFF_NoSettings() );
            transitionRules.put( VendorInfoState.NFF, factory.createVendorInfoSetterForNFF_NoSettings() );
            transitionRules.put( VendorInfoState.GFF, factory.createVendorInfoSetterForGFF_NoSettings() );
        }
    }

    /**
     * @see StateMachineProcessor#process(org.apache.maven.dotnet.vendor.VendorInfo)
     */
    public void process( VendorInfo vendorInfo )
        throws IllegalStateException
    {
        VendorInfoState startState = VendorInfoState.START.getState( vendorInfo );
        VendorInfoTransitionRule rule = transitionRules.get( startState );
        if ( rule == null )
        {
            throw new IllegalStateException(
                "NPANDAY-102-002: Could not find rule for state: State = " + startState.name() );
        }
        for ( VendorInfoState state = VendorInfoState.START; !state.equals( VendorInfoState.EXIT ); )
        {
            state = rule.process( vendorInfo );
            rule = transitionRules.get( state );
            if ( rule == null && !state.equals( VendorInfoState.EXIT ) )
            {
                throw new IllegalStateException(
                    "NPANDAY-102-003: Could not find rule for state: State = " + state.name() );
            }
        }
    }
}
