package npanday.executable.impl;

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

import npanday.executable.ExecutableCapability;
import npanday.executable.ExecutableMatchPolicy;
import npanday.executable.compiler.CompilerCapability;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Creates executable match policies.
 *
 * @author Shane Isbell
 */
final class MatchPolicyFactory
{

    /**
     * Constructor
     */
    private MatchPolicyFactory()
    {
    }

    /**
     * Creates a policy to match profiles.
     *
     * @param profile
     * @return a policy to match profiles.
     */
    static ExecutableMatchPolicy createProfilePolicy( final String profile )
    {
        return new ExecutableMatchPolicy()
        {
            public boolean match( ExecutableCapability executableCapability )
            {
                String p = executableCapability.getProfile().toLowerCase().trim();
                return ( profile.toLowerCase().trim().equals( p ) );
            }

            public String toString()
            {
                return "ExecutableMatchPolicy[profile: '" + profile + "']";
            }
        };
    }

    /**
     * Creates a policy to match the operating system
     *
     * @param operatingSystem
     * @return a policy to match the operating system
     */
    static ExecutableMatchPolicy createOperatingSystemPolicy( final String operatingSystem )
    {
        return new ExecutableMatchPolicy()
        {
            public boolean match( ExecutableCapability executableCapability )
            {
                String os = executableCapability.getOperatingSystem().toLowerCase().trim();
                return ( operatingSystem.toLowerCase().trim().contains( os ) );
            }

            public String toString()
            {
                return "ExecutableMatchPolicy[operatingSystem: '" + operatingSystem + "']";
            }
        };
    }

    /**
     * Creates a match policy to match the .NET language
     *
     * @param language
     * @return a match policy to match the .NET language
     */
    static ExecutableMatchPolicy createLanguagePolicy( final String language )
    {
        return new ExecutableMatchPolicy()
        {
            public boolean match( ExecutableCapability executableCapability )
            {
                if ( !( executableCapability instanceof CompilerCapability ) )
                {
                    return true;
                }
                CompilerCapability capability = (CompilerCapability) executableCapability;
                return language.toLowerCase().trim().equals( capability.getLanguage().toLowerCase().trim() );
            }

            public String toString()
            {
                return "ExecutableMatchPolicy[language: '" + language + "']";
            }
        };
    }

    public static ExecutableMatchPolicy createExecutableVersionPolicy( final String requiredExecutableVersion )
    {
        return new ExecutableMatchPolicy()
        {
            public boolean match( ExecutableCapability executableCapability )
            {
                // if not specified, all versions are valid
                if (isNullOrEmpty(requiredExecutableVersion))
                    return true;

                String offeredExecutableVersion = executableCapability.getExecutableVersion();

                // if not specified, it is valid for all versions!
                if (isNullOrEmpty( offeredExecutableVersion ))
                    return true;

                String required = requiredExecutableVersion.toLowerCase().trim();
                offeredExecutableVersion = offeredExecutableVersion.toLowerCase().trim();
                try {
                    VersionRange range = VersionRange.createFromVersionSpec( offeredExecutableVersion );
                    return range.containsVersion( new DefaultArtifactVersion( required ) );
                } catch (InvalidVersionSpecificationException e) {
                    // fallback to just matching version if not a valid range
                    return required.equals(offeredExecutableVersion);
                }
            }

            public String toString()
            {
                return "ExecutableMatchPolicy[executableVersion: '" + requiredExecutableVersion + "']";
            }
        };
    }
}
