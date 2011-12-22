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

import npanday.vendor.VendorInfo;

import java.util.List;

/**
 * Provides information about the capability of a specific executable or compiler plugin. The framework will match this
 * capability to an <code>ExecutableRequirement</code>. The framework can then use the
 * <code>getPluginClassName</code> method to instantiate an appropriate class that is capable of
 * supporting the <code>ExecutableRequirement</code>.
 *
 * Executable capabilities are specified within the executable-plugins.xml file.
 *
 * @author Shane Isbell
 * @see ExecutableRequirement
 * @see CapabilityMatcher
 */
public interface ExecutableCapability
{

    /**
     * Returns the command options that the executable is capable of supporting.
     *
     * @return the command capability that the executable is capable of supporting. This capability is used to determine
     *         the command line parameters that the executable supports.
     */
    CommandCapability getCommandCapability();

    /**
     * Returns the ID for this capability.
     *
     * @return the ID for this capability
     */
    String getIdentifier();

    /**
     * Returns the vendor capability of the executable
     *
     * @return the vendor capability of the executable
     */
    VendorInfo getVendorInfo();

    /**
     * Returns the supported profile. A profile is used to differentiate a capability beyond the standard
     * vendor/OS/profile/frameworkVersion parameters.
     *
     * @return the supported profile
     */
    String getProfile();

    /**
     * Returns the operating system that the executable is capable of supporting.
     *
     * @return the operating system that the executable is capable of supporting
     */
    String getOperatingSystem();

    /**
     * Returns the architecture that the executable is capable of supporting.
     *
     * @return the architecture that the executable is capable of supporting
     */
    String getArchitecture();

    /**
     * Returns a list of all .NET frameworks versions that the executable is capable of supporting.
     *
     * @return a list of all .NET frameworks versions that the executable is capable of supporting
     */
    List<String> getFrameworkVersions();

    /**
     * Returns the executable as it is given on the commandline.
     *
     * @return the executable as it is given on the commandline
     */
    String getExecutableName();

    /**
     * Returns the version of the executable that is offered as capability.
     */
    String getExecutableVersion();

    /**
     * Returns the class name of the executable plugin that knows how to handle the execution request.
     *
     * @return the class name of the executable plugin that knows how to handle the execution request
     */
    String getPluginClassName();

    /**
     * Returns the net dependency id (within the net-dependencies.xml file).
     *
     * @return the net dependency id
     */
    String getNetDependencyId();

    /**
     * Retrieves the paths, the executable is most likely to be found on. Can contain
     * both existing and not existing paths.
     */
    List<String> getProbingPaths();
}

