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
package npanday.executable.impl;

import npanday.PlatformUnsupportedException;
import npanday.executable.CapabilityMatcher;
import npanday.executable.ExecutableCapability;
import npanday.executable.ExecutableMatchPolicy;
import npanday.executable.ExecutableRequirement;
import npanday.executable.compiler.CompilerCapability;
import npanday.executable.compiler.CompilerRequirement;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.StateMachineProcessor;
import npanday.vendor.VendorInfo;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation of the <code>CapabilityMatcher</code> interface.
 *
 * @author Shane Isbell
 * @plexus.component
 *   role="npanday.executable.CapabilityMatcher"
 */
public class CapabilityMatcherImpl
    extends AbstractLogEnabled
    implements CapabilityMatcher
{
    /**
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * @plexus.requirement
     */
    private StateMachineProcessor processor;



    public CompilerCapability matchCompilerCapabilityFor( CompilerRequirement compilerRequirement,
                                                          List<ExecutableMatchPolicy> matchPolicies )
        throws PlatformUnsupportedException
    {
        if ( compilerRequirement == null )
        {
            throw new PlatformUnsupportedException( "NPANDAY-065-006: The compiler requirement should not be null." );
        }

        if ( matchPolicies == null )
        {
            matchPolicies = new ArrayList<ExecutableMatchPolicy>();
        }

        VendorInfo vendorInfo = matchVendorInfo(compilerRequirement);
        if ( matchPolicies == null )
        {
            matchPolicies = new ArrayList<ExecutableMatchPolicy>();
        }

        matchPolicies.add( MatchPolicyFactory.createOperatingSystemPolicy( System.getProperty( "os.name" ) ) );
        matchPolicies.add( MatchPolicyFactory.createLanguagePolicy( compilerRequirement.getLanguage() ) );
        matchPolicies.add( MatchPolicyFactory.createProfilePolicy( compilerRequirement.getProfile() ) );

        return (CompilerCapability) matchFromExecutableCapabilities( getCompilerCapabilities(vendorInfo), matchPolicies );
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
     * @throws npanday.PlatformUnsupportedException
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
        VendorInfo vendorInfo = matchVendorInfo(executableRequirement);
        if ( matchPolicies == null )
        {
            matchPolicies = new ArrayList<ExecutableMatchPolicy>();
        }

        matchPolicies.add( MatchPolicyFactory.createOperatingSystemPolicy( System.getProperty( "os.name" ) ) );
        matchPolicies.add( MatchPolicyFactory.createProfilePolicy( executableRequirement.getProfile() ) );

        return matchFromExecutableCapabilities( getExecutableCapabilities(vendorInfo), matchPolicies );
    }


    private ExecutableCapability matchFromExecutableCapabilities( List<ExecutableCapability> executableCapabilities,
                                                                  List<ExecutableMatchPolicy> matchPolicies )
        throws PlatformUnsupportedException
    {
        for ( ExecutableCapability executableCapability : executableCapabilities )
        {
            if ( matchExecutableCapability( executableCapability, matchPolicies ) )
            {
                getLogger().debug( "NPANDAY-065-001: Found matching capability: " + executableCapability );
                return executableCapability;
            }
            else
            {
                getLogger().debug( "NPANDAY-065-005: Capability doesn't match: " + executableCapability );
            }
        }
        throw new PlatformUnsupportedException(
            "NPANDAY-065-002: Could not match any of the " + executableCapabilities.size() + " capabilities with "
                + matchPolicies );
    }

    private VendorInfo matchVendorInfo(ExecutableRequirement executableRequirement)
        throws PlatformUnsupportedException
    {
       VendorInfo vendorInfo;
        try
        {
            vendorInfo = processor.process( executableRequirement );
            getLogger().debug(
                String.format( "NPANDAY-065-008: Found vendor %s for requirement %s", vendorInfo, executableRequirement ) );
            return vendorInfo;
        }
        catch ( npanday.vendor.IllegalStateException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-065-007: Error when searching vendor with " + executableRequirement, e );
        }
    }

    private boolean matchExecutableCapability( ExecutableCapability executableCapability,
                                               List<ExecutableMatchPolicy> matchPolicies )
    {
        for ( ExecutableMatchPolicy executableMatchPolicy : matchPolicies )
        {
            boolean match = executableMatchPolicy.match( executableCapability );
            if ( !match )
            {
                getLogger().debug( "NPANDAY-065-009: Failed to match policy: " + executableMatchPolicy );
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
     * @param vendorInfo
     */
    private List<ExecutableCapability> getCompilerCapabilities( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        CompilerPluginsRepository pluginsRepository =
            (CompilerPluginsRepository) repositoryRegistry.find( "compiler-plugins" );

        List<ExecutableCapability> primary = new ArrayList<ExecutableCapability>();
        if ( pluginsRepository != null )
        {
            primary = pluginsRepository.getCompilerCapabilities(vendorInfo);
        }
        if ( primary.isEmpty() )
        {
            throw new PlatformUnsupportedException( "NPANDAY-065-003: No compiler capabilities configured" );
        }
        return primary;
    }


    /**
     * Returns all platform capabilities (as defined in the compiler-plugins.xml file). This is more than the platform
     * capabilities for the invoking platform.
     *
     * @return all platform capabilities (as defined in the compiler-plugins.xml file).
     * @param vendorInfo
     */
    private List<ExecutableCapability> getExecutableCapabilities( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        ExecutablePluginsRepository pluginsRepository =
            (ExecutablePluginsRepository) repositoryRegistry.find( "executable-plugins" );

        List<ExecutableCapability> primary =  pluginsRepository.getCapabilities(vendorInfo);

        if ( primary.isEmpty() )
        {
            throw new PlatformUnsupportedException( "NPANDAY-065-004: No executable capabilities configured" );
        }
        return primary;
    }

}
