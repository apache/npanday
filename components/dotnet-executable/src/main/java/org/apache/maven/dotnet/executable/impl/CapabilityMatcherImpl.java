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
package org.apache.maven.dotnet.executable.impl;

import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.executable.*;
import org.apache.maven.dotnet.executable.compiler.*;

import java.util.List;
import java.util.ArrayList;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.apache.maven.dotnet.registry.RepositoryRegistry;

/**
 * Provides an implementation of the <code>CapabilityMatcher</code> interface.
 *
 * @author Shane Isbell
 */
public class CapabilityMatcherImpl
    implements CapabilityMatcher, LogEnabled
{
    /**
     * A logger for writing log messages
     */
    private Logger logger;

    private RepositoryRegistry repositoryRegistry;

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    public CompilerCapability matchCompilerCapabilityFor( CompilerRequirement compilerRequirement,
                                                          List<ExecutableMatchPolicy> matchPolicies )
        throws PlatformUnsupportedException
    {
        if ( compilerRequirement == null )
        {
            throw new PlatformUnsupportedException( "NMAVEN-065-006: The compiler requirement should not be null." );
        }
        if ( matchPolicies == null )
        {
            matchPolicies = new ArrayList<ExecutableMatchPolicy>();
        }
        matchPolicies.add( MatchPolicyFactory.createOperatingSystemPolicy( System.getProperty( "os.name" ) ) );
        matchPolicies.add( MatchPolicyFactory.createVendorPolicy( compilerRequirement.getVendor() ) );
        matchPolicies.add( MatchPolicyFactory.createLanguagePolicy( compilerRequirement.getLanguage() ) );
        matchPolicies.add(
            MatchPolicyFactory.createFrameworkVersionPolicy( compilerRequirement.getFrameworkVersion() ) );
        matchPolicies.add( MatchPolicyFactory.createProfilePolicy( compilerRequirement.getProfile() ) );
        return (CompilerCapability) matchFromExecutableCapabilities( getCompilerCapabilities(), matchPolicies );
    }

    public CompilerCapability matchCompilerCapabilityFor( CompilerRequirement compilerRequirement )
        throws PlatformUnsupportedException
    {
        return matchCompilerCapabilityFor( compilerRequirement, new ArrayList<ExecutableMatchPolicy>() );
    }

    /**
     * Returns the <code>PlatformCapability</code> for the given vendor and language.
     *
     * @param executableRequirement
     * @return the <code>PlatformCapability</code> for the given vendor and language.
     * @throws org.apache.maven.dotnet.PlatformUnsupportedException
     *          if the vendor and language (as specified in plugin-compilers.xml)
     *          are not available for the invoking operating system.
     */
    public ExecutableCapability matchExecutableCapabilityFor( ExecutableRequirement executableRequirement )
        throws PlatformUnsupportedException
    {
        return matchExecutableCapabilityFor( executableRequirement, new ArrayList<ExecutableMatchPolicy>() );
    }

    /**
     * Returns the <code>PlatformCapability</code> for the given vendor, language and match policies. For this query to return
     * a platform capability ALL must match (general AND condition). The matchPolicies allow the developer to customize
     * the query results by any of the PlatformCapability properties.
     *
     * @param executableRequirement
     * @param matchPolicies         policies for matching capabilities
     * @return the <code>PlatformCapability</code> for the given vendor, language and match policies.
     * @throws PlatformUnsupportedException if the vendor and language (as specified in plugin-compilers.xml)
     *                                      are not available for the invoking operating system and/or any of the match policies fail.
     */
    public ExecutableCapability matchExecutableCapabilityFor( ExecutableRequirement executableRequirement,
                                                              List<ExecutableMatchPolicy> matchPolicies )
        throws PlatformUnsupportedException
    {
        if ( matchPolicies == null )
        {
            matchPolicies = new ArrayList<ExecutableMatchPolicy>();
        }
        matchPolicies.add( MatchPolicyFactory.createOperatingSystemPolicy( System.getProperty( "os.name" ) ) );
        matchPolicies.add( MatchPolicyFactory.createVendorPolicy( executableRequirement.getVendor() ) );
        matchPolicies.add(
            MatchPolicyFactory.createFrameworkVersionPolicy( executableRequirement.getFrameworkVersion() ) );
        matchPolicies.add( MatchPolicyFactory.createProfilePolicy( executableRequirement.getProfile() ) );
        return matchFromExecutableCapabilities( getExecutableCapabilities(), matchPolicies );
    }


    private ExecutableCapability matchFromExecutableCapabilities( List<ExecutableCapability> executableCapabilities,
                                                                  List<ExecutableMatchPolicy> matchPolicies )
        throws PlatformUnsupportedException
    {
        for ( ExecutableCapability executableCapability : executableCapabilities )
        {
            logger.debug( "NMAVEN-065-005: Attempting to match capability: " + executableCapability );
            if ( matchExecutableCapability( executableCapability, matchPolicies ) )
            {
                logger.debug( "NMAVEN-065-001: Made a Platform Capability Match: " + executableCapability );
                return executableCapability;
            }
        }
        throw new PlatformUnsupportedException( "NMAVEN-065-002: Could not match platform: OS = " +
            System.getProperty( "os.name" ) + ", Number of Capabilities = " + executableCapabilities.size() +
            ", Number of Policies = " + matchPolicies.size() );
    }


    private boolean matchExecutableCapability( ExecutableCapability executableCapability,
                                               List<ExecutableMatchPolicy> matchPolicies )
    {
        for ( ExecutableMatchPolicy executableMatchPolicy : matchPolicies )
        {
            boolean match = executableMatchPolicy.match( executableCapability );
            if ( !match )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns all platform capabilities (as defined in the compiler-plugins.xml file). This is more than the platform
     * capabilities for the invoking platform.
     *
     * @return all platform capabilities (as defined in the compiler-plugins.xml file).
     */
    private List<ExecutableCapability> getCompilerCapabilities()
        throws PlatformUnsupportedException
    {
        CompilerPluginsRepository pluginsRepository =
            (CompilerPluginsRepository) repositoryRegistry.find( "compiler-plugins" );
        CompilerPluginsRepository pluginsRepositoryExt =
            (CompilerPluginsRepository) repositoryRegistry.find( "compiler-plugins-ext" );
        List<ExecutableCapability> primary = new ArrayList<ExecutableCapability>();
        if ( pluginsRepository != null )
        {
            primary = pluginsRepository.getCompilerCapabilities();
        }

        if ( pluginsRepositoryExt != null )
        {
            primary.addAll( pluginsRepositoryExt.getCompilerCapabilities() );
        }
        if ( primary.isEmpty() )
        {
            throw new PlatformUnsupportedException( "NMAVEN-065-003: No compiler capabilities configured" );
        }
        return primary;
    }


    /**
     * Returns all platform capabilities (as defined in the compiler-plugins.xml file). This is more than the platform
     * capabilities for the invoking platform.
     *
     * @return all platform capabilities (as defined in the compiler-plugins.xml file).
     */
    private List<ExecutableCapability> getExecutableCapabilities()
        throws PlatformUnsupportedException
    {
        ExecutablePluginsRepository pluginsRepository =
            (ExecutablePluginsRepository) repositoryRegistry.find( "executable-plugins" );
        ExecutablePluginsRepository pluginsRepositoryExt =
            (ExecutablePluginsRepository) repositoryRegistry.find( "executable-plugins-ext" );
        List<ExecutableCapability> primary = new ArrayList<ExecutableCapability>();
        if ( pluginsRepository != null )
        {
            primary = pluginsRepository.getCapabilities();
        }

        if ( pluginsRepositoryExt != null )
        {
            primary.addAll( pluginsRepositoryExt.getCapabilities() );
        }
        if ( primary.isEmpty() )
        {
            throw new PlatformUnsupportedException( "NMAVEN-065-004: No executable capabilities configured" );
        }
        return primary;
    }

}
