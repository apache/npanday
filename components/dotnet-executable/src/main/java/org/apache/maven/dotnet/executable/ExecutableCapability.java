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
package npanday.executable;

import npanday.vendor.Vendor;
import npanday.executable.compiler.CompilerCapability;

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
     * Sets the command options that the executable is capable of supporting.
     *
     * @param commandCapability the command capability, which is used to determine the command line parameters that
     *                          the executable supports.
     */
    void setCommandCapability( CommandCapability commandCapability );

    /**
     * Returns the ID for this capability.
     *
     * @return the ID for this capability
     */
    String getIdentifier();

    /**
     * Sets the ID for this capability.
     *
     * @param identifier the ID of the capability
     */
    void setIdentifier( String identifier );

    /**
     * Returns the vendor capability of the executable: currently only - MS, MONO, DotGNU
     *
     * @return the vendor capability of the executable
     */
    Vendor getVendor();

    /**
     * Sets the vendor capability of the executable: currently only - MS, MONO, DotGNU
     *
     * @param vendor
     */
    void setVendor( Vendor vendor );

    /**
     * Returns the supported profile. A profile is used to differentiate a capability beyond the standard
     * vendor/OS/profile/frameworkVersion parameters.
     *
     * @return the supported profile
     */
    String getProfile();

    /**
     * Sets the supported profile. A profile is used to differentiate a capability beyond the standard
     * vendor/OS/profile/frameworkVersion parameters.
     *
     * @param profile the profile of the executable.
     */
    void setProfile( String profile );

    /**
     * Returns the operating system that the executable is capable of supporting.
     *
     * @return the operating system that the executable is capable of supporting
     */
    String getOperatingSystem();

    /**
     * Sets the operating system that the executable is capable of supporting.
     *
     * @param operatingSystem
     */
    void setOperatingSystem( String operatingSystem );

    /**
     * Returns the architecture that the executable is capable of supporting.
     *
     * @return the architecture that the executable is capable of supporting
     */
    String getArchitecture();

    /**
     * Sets the architecture that the executable is capable of supporting.
     *
     * @param architecture the architecture that the executable is capable of supporting
     */
    void setArchitecture( String architecture );

    /**
     * Returns a list of all .NET frameworks versions that the executable is capable of supporting.
     *
     * @return a list of all .NET frameworks versions that the executable is capable of supporting
     */
    List<String> getFrameworkVersions();

    /**
     * Sets a list of all .NET frameworks versions that the executable is capable of supporting.
     *
     * @param frameworkVersions a list of all .NET frameworks versions that the executable is capable of supporting
     */
    void setFrameworkVersions( List<String> frameworkVersions );

    /**
     * Returns the executable as it is given on the commandline.
     *
     * @return the executable as it is given on the commandline
     */
    String getExecutable();

    /**
     * Sets the executable as it is given on the commandline.
     *
     * @param executable the executable as it is given on the commandline
     */
    void setExecutable( String executable );

    /**
     * Returns the class name of the executable plugin that knows how to handle the execution request.
     *
     * @return the class name of the executable plugin that knows how to handle the execution request
     */
    String getPluginClassName();

    /**
     * Sets the class name of the executable plugin that knows how to handle the execution request.
     *
     * @param pluginClassName the class name of the executable plugin that knows how to handle the execution request
     */
    void setPluginClassName( String pluginClassName );

    /**
     * Returns the net dependency id (within the net-dependencies.xml file).
     *
     * @return the net dependency id
     */
    String getNetDependencyId();

    /**
     * Sets the net dependency id.
     *
     * @param netDependencyId
     */
    void setNetDependencyId(String netDependencyId);

    /**
     * Provides factory services for creating a default instance of the executable capability.
     */
    public static class Factory
    {
        /**
         * Default constructor
         */
        private Factory()
        {
        }

        /**
         * Returns a default instance of the executable capability.
         *
         * @return a default instance of the executable capability
         */
        public static ExecutableCapability createDefaultExecutableCapability()
        {
            return new CompilerCapability()
            {

                private Vendor vendor;

                private String language;

                private String operatingSystem;

                private String architecture;

                private boolean hasJustInTime;

                private String pluginClassName;

                private String executable;

                private String identifier;

                private CommandCapability commandCapability;

                private List<String> frameworkVersions;

                private List<String> coreAssemblies;

                private String profile;

                private String assemblyPath;

                private String netDependencyId;

                public String getAssemblyPath()
                {
                    return assemblyPath;
                }

                public void setAssemblyPath( String assemblyPath )
                {
                    this.assemblyPath = assemblyPath;
                }

                public String getProfile()
                {
                    return profile;
                }

                public void setProfile( String profile )
                {
                    this.profile = profile;
                }

                public List<String> getCoreAssemblies()
                {
                    return coreAssemblies;
                }

                public void setCoreAssemblies( List<String> coreAssemblies )
                {
                    this.coreAssemblies = coreAssemblies;
                }

                public List<String> getFrameworkVersions()
                {
                    return frameworkVersions;
                }

                public void setFrameworkVersions( List<String> frameworkVersions )
                {
                    this.frameworkVersions = frameworkVersions;
                }

                public String getIdentifier()
                {
                    return identifier;
                }

                public void setIdentifier( String identifier )
                {
                    this.identifier = identifier;
                }

                public String getExecutable()
                {
                    return executable;
                }

                public void setExecutable( String executable )
                {
                    this.executable = executable;
                }

                public Vendor getVendor()
                {
                    return vendor;
                }

                public void setVendor( Vendor vendor )
                {
                    this.vendor = vendor;
                }

                public String getLanguage()
                {
                    return language;
                }

                public void setLanguage( String language )
                {
                    this.language = language;
                }

                public String getOperatingSystem()
                {
                    return operatingSystem;
                }

                public void setOperatingSystem( String operatingSystem )
                {
                    this.operatingSystem = operatingSystem;
                }

                public String getArchitecture()
                {
                    return architecture;
                }

                public void setArchitecture( String architecture )
                {
                    this.architecture = architecture;
                }

                public boolean isHasJustInTime()
                {
                    return hasJustInTime;
                }

                public void setHasJustInTime( boolean hasJustInTime )
                {
                    this.hasJustInTime = hasJustInTime;
                }


                public String getPluginClassName()
                {
                    return pluginClassName;
                }

                public void setPluginClassName( String pluginClassName )
                {
                    this.pluginClassName = pluginClassName;
                }

                public CommandCapability getCommandCapability()
                {
                    return commandCapability;
                }


                public void setCommandCapability( CommandCapability commandCapability )
                {
                    this.commandCapability = commandCapability;
                }

                public String getNetDependencyId()
                {
                    return netDependencyId;
                }

                public void setNetDependencyId( String executableLocation )
                {
                    this.netDependencyId = executableLocation;
                }

                public String toString()
                {
                    return "ID = " + identifier + ", Plugin Class: " + pluginClassName + ", OS = " + operatingSystem +
                        ", Language = " + language + ", Vendor = " + vendor;
                }
            };
        }
    }

}
