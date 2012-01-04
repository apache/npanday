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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import npanday.vendor.VendorInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Holds the configured executable capability.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
// TODO: Refactor to be based on the configured plugins
public class MutableExecutableCapability
    implements ExecutableCapability
{
    protected VendorInfo vendorInfo;

    protected String operatingSystem;

    private String architecture;

    protected String pluginClassName;

    protected Properties pluginConfiguration;

    private String executable;

    private String executableVersion;

    protected String identifier;

    private CommandCapability commandCapability;

    private List<String> frameworkVersions;

    private String profile;

    private String netDependencyId;

    private List<String> probingPaths;

    public String getProfile()
    {
        return profile;
    }

    public void setProfile( String profile )
    {
        this.profile = profile;
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

    public String getExecutableName()
    {
        return executable;
    }

    public void setExecutableName( String executableName )
    {
        this.executable = executableName;
    }

    public String getExecutableVersion()
    {
        return executableVersion;
    }

    public void setExectuableVersion( String executableVersion )
    {
        this.executableVersion = executableVersion;
    }

    public VendorInfo getVendorInfo()
    {
        return vendorInfo;
    }

    public void setVendorInfo( VendorInfo vendorInfo )
    {
        this.vendorInfo = vendorInfo;
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

    public String getPluginClassName()
    {
        return pluginClassName;
    }

    public void setPluginClassName( String pluginClassName )
    {
        this.pluginClassName = pluginClassName;
    }

    public Properties getPluginConfiguration()
    {
        return pluginConfiguration;
    }

    public void setPluginConfiguration( Properties pluginConfiguration )
    {
        this.pluginConfiguration = pluginConfiguration;
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

    public List<String> getProbingPaths()
    {
        // if probing paths are defined fot the capability, these are to be
        // used when searching executables.
        if ( probingPaths != null && !probingPaths.isEmpty() )
        {
            return probingPaths;
        }

        // if not, we expect the executable is provided by the vendor
        final List<File> vendorPaths = checkNotNull(
            getVendorInfo(), "Vendor info is unavailable"
        ).getExecutablePaths();

        List<String> vendorPathsAsString = Lists.transform(
            vendorPaths, new Function<File, String>()
            {
                public String apply( @Nullable File file )
                {
                    return checkNotNull( file, "file was null").toString();
                }
            }
        );

        return Collections.unmodifiableList( vendorPathsAsString );
    }

    public void setProbingPaths( List<String> probingPaths )
    {
        this.probingPaths = probingPaths;
    }

    @Override
    public String toString()
    {
        return "ExecutableCapability [" + "vendorInfo=" + vendorInfo + ", operatingSystem='" + operatingSystem
            + '\'' + ", profile='" + profile + '\'' + ']';
    }
}
