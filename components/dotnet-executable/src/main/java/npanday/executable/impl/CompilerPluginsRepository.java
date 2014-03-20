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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import npanday.executable.CommandCapability;
import npanday.executable.ExecutableCapability;
import npanday.executable.compiler.MutableCompilerCapability;
import npanday.model.compiler.plugins.*;
import npanday.model.compiler.plugins.io.xpp3.CompilerPluginXpp3Reader;
import npanday.registry.ModelInterpolator;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import npanday.registry.impl.AbstractMultisourceRepository;
import npanday.vendor.VendorInfo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Repository for reading and providing access to the compiler-plugins.xml config file.
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component
 *   role="npanday.executable.impl.CompilerPluginsRepository"
 */
public final class CompilerPluginsRepository
    extends AbstractMultisourceRepository<CompilerPluginsModel>
    implements Repository
{
    /**
     * List<npanday.model.compiler.plugins.CompilerPlugin> of compiler plugins pulled from the
     * various compiler-plugins.xml files.
     */
    private List<CompilerPlugin> compilerPlugins = new ArrayList<CompilerPlugin>();

    @Override
    protected CompilerPluginsModel loadFromReader( Reader reader, Hashtable properties )
        throws IOException, XmlPullParserException
    {
        CompilerPluginXpp3Reader xpp3Reader = new CompilerPluginXpp3Reader();
        return xpp3Reader.read( reader );
    }

    @Override
    protected void mergeLoadedModel( CompilerPluginsModel model )
        throws NPandayRepositoryException
    {
        compilerPlugins.addAll( model.getCompilerPlugins() );
    }

    /**
     * Remove all stored values in preparation for a reload.
     */
    @Override
    protected void clear()
    {
        compilerPlugins.clear();
    }

    /**
     * Returns config information as list of platform capabilities.
     *
     * @return config information as list of platform capabilities.
     * @param vendorInfo
     */
    List<ExecutableCapability> getCompilerCapabilities( final VendorInfo vendorInfo )
    {
        List<ExecutableCapability> platformCapabilities = new ArrayList<ExecutableCapability>();
        for (CompilerPlugin plugin : compilerPlugins) {
            if (plugin.getVendor() != null && !vendorInfo.getVendor().getVendorName().toLowerCase().equals(plugin.getVendor().toLowerCase()))
                continue;

            if (VersionComparer.isVendorVersionMissmatch(plugin.getVendorVersion(), vendorInfo.getVendorVersion())) {
                continue;
            }

            if (VersionComparer.isFrameworkVersionMissmatch(plugin.getFrameworkVersions(), vendorInfo.getFrameworkVersion())) {
                continue;
            }

            for (Platform platform : plugin.getPlatforms()) {
                if (plugin.getProfiles() != null && !plugin.getProfiles().isEmpty()) {
                    for (Profile profile : plugin.getProfiles()) {
                        MutableCompilerCapability platformCapability = createPlatformCapability(vendorInfo, plugin, platform);
                        platformCapability.setProfile(profile.getId());
                        if (!isNullOrEmpty(profile.getDefaultAssemblyPath())) {
                            platformCapability.setAssemblyPath(new File(profile.getDefaultAssemblyPath()));
                        }
                        platformCapability.setTargetFramework(profile.getTargetFramework());
                        platformCapability.setCoreAssemblies(profile.getAssemblies());

                        platformCapabilities.add(platformCapability);
                    }
                } else {
                    MutableCompilerCapability platformCapability = createPlatformCapability(vendorInfo, plugin, platform);
                    platformCapability.setProfile("FULL");
                    platformCapabilities.add(platformCapability);
                }
            }
        }
        return platformCapabilities;
    }

    private static MutableCompilerCapability createPlatformCapability(VendorInfo vendorInfo, CompilerPlugin plugin, Platform platform) {
        MutableCompilerCapability platformCapability = new MutableCompilerCapability();

        platformCapability.setVendorInfo( vendorInfo );
        platformCapability.setProbingPaths(plugin.getProbingPaths());

        String os = platform.getOperatingSystem();

        platformCapability.setLanguage(plugin.getLanguage());
        platformCapability.setOperatingSystem( os );
        platformCapability.setPluginClassName(plugin.getPluginClass());
        platformCapability.setPluginConfiguration(plugin.getPluginConfiguration());

        platformCapability.setExecutableName(plugin.getExecutable());
        platformCapability.setIdentifier(plugin.getIdentifier());
        platformCapability.setFrameworkVersions(plugin.getFrameworkVersions());
        String arch = platform.getArchitecture();
        CommandFilter filter = plugin.getCommandFilter();

        List<String> includes = ( filter != null ) ? filter.getIncludes() : new ArrayList<String>();
        List<String> excludes = ( filter != null ) ? filter.getExcludes() : new ArrayList<String>();
        platformCapability.setCommandCapability(
            CommandCapability.Factory.createDefaultCommandCapability( includes, excludes ) );
        if ( arch != null )
        {
            platformCapability.setArchitecture( arch );
        }
        return platformCapability;
    }

    // ### COMPONENTS REQUIRED BY THE BASE CLASS

    /**
     * @plexus.requirement
     */
    private ModelInterpolator interpolator;

    @Override
    protected ModelInterpolator getInterpolator()
    {
        return interpolator;
    }
}
