package npanday.executable;

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

import npanday.PlatformUnsupportedException;
import npanday.executable.compiler.CompilerRequirement;
import npanday.executable.compiler.CompilerCapability;

import java.util.List;

/**
 * Provides factory services for creating capabilities (based on user-defined requirements) for compilers and executables.
 *
 * @author Shane Isbell
 */
public interface CapabilityMatcher
{

    /**
     * Returns executable capability for the specified requirement. This method looks at the requirement of the user and returns
     * an executable that is capable of meeting those requirements.
     *
     * @param executableRequirement the requirements for the executable
     * @return executable capability for the specified requirement.
     * @throws npanday.PlatformUnsupportedException if the requirements cannot be met by the platform
     *
     */
    ExecutableCapability matchExecutableCapabilityFor( ExecutableRequirement executableRequirement )
        throws PlatformUnsupportedException;

    /**
     * Returns executable capability for the specified requirement and match policies. Use this to customize matches beyond
     * the standard vendor/OS/profile/frameworkVersion parameters.
     *
     * @param executableRequirement the requirements for the executable
     * @param matchPolicies the policies used to specialize the match
     * @return executable capability for the specified requirement and match policies.
     * @throws npanday.PlatformUnsupportedException if the requirements cannot be met by the platform
     *
     */
    ExecutableCapability matchExecutableCapabilityFor( ExecutableRequirement executableRequirement,
                                                       List<ExecutableMatchPolicy> matchPolicies )
        throws PlatformUnsupportedException;


    /**
     * Returns compiler capability for the specified requirement. This method looks at the requirement of the user and returns
     * a compiler that is capable of meeting those requirements.
     *
     * @param compilerRequirement the requirements for the compiler
     * @return compiler capability for the specified requirement.
     * @throws npanday.PlatformUnsupportedException if the requirements cannot be met by the platform
     *
     */

    CompilerCapability matchCompilerCapabilityFor( CompilerRequirement compilerRequirement )
        throws PlatformUnsupportedException;

    /**
     * Returns compiler capability for the specified requirement and match policies. Use this to customize matches beyond
     * the standard vendor/OS/profile/language/frameworkVersion parameters.
     *
     * @param compilerRequirement requirements for the compiler
     * @param matchPolicies the policies used to specialize the match
     * @return compiler capability for the specified requirement and match policies.
     * @throws PlatformUnsupportedException if the requirements cannot be met by the platform
     */
    CompilerCapability matchCompilerCapabilityFor( CompilerRequirement compilerRequirement,
                                                   List<ExecutableMatchPolicy> matchPolicies )
        throws PlatformUnsupportedException;

}
