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
import npanday.model.compiler.plugins.CommandFilter;
import npanday.model.compiler.plugins.CompilerPlugin;
import npanday.model.compiler.plugins.CompilerPluginsModel;
import npanday.model.compiler.plugins.Platform;
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
    private List compilerPlugins = new ArrayList();

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
        for ( Iterator i = compilerPlugins.iterator(); i.hasNext(); )
        {
            CompilerPlugin plugin = (CompilerPlugin) i.next();
            String language = plugin.getLanguage();
            String pluginClassName = plugin.getPluginClass();
            Properties pluginConfiguration = plugin.getPluginConfiguration();
            String executable = plugin.getExecutable();
            String vendor = plugin.getVendor();
            String vendorVersion = plugin.getVendorVersion();
            String identifier = plugin.getIdentifier();
            String profile = plugin.getProfile();
            List<String> frameworkVersions = plugin.getFrameworkVersions();
            List<String> coreAssemblies = plugin.getAssemblies();
            String defaultAssemblyPath = plugin.getDefaultAssemblyPath();
            String targetFramework = plugin.getTargetFramework();

            if (vendor != null && !vendorInfo.getVendor().getVendorName().toLowerCase().equals( vendor.toLowerCase() ))
                continue;

            if ( VersionComparer.isVendorVersionMissmatch(vendorVersion, vendorInfo.getVendorVersion()) )
            {
                continue;
            }

            if ( VersionComparer.isFrameworkVersionMissmatch(frameworkVersions, vendorInfo.getFrameworkVersion()) )
            {
                continue;
            }

            List platforms = plugin.getPlatforms();
            for ( Iterator j = platforms.iterator(); j.hasNext(); )
            {
                MutableCompilerCapability platformCapability = new MutableCompilerCapability();

                platformCapability.setVendorInfo( vendorInfo );
                platformCapability.setProbingPaths(plugin.getProbingPaths());

                Platform platform = (Platform) j.next();
                String os = platform.getOperatingSystem();

                platformCapability.setLanguage( language );
                platformCapability.setOperatingSystem( os );
                platformCapability.setPluginClassName( pluginClassName );
                platformCapability.setPluginConfiguration( pluginConfiguration );

                platformCapability.setExecutableName( executable );
                platformCapability.setIdentifier( identifier );
                platformCapability.setFrameworkVersions( frameworkVersions );
                platformCapability.setProfile( profile );
                if (!isNullOrEmpty(defaultAssemblyPath))
                {
                    platformCapability.setAssemblyPath( new File(defaultAssemblyPath) );
                }
                platformCapability.setTargetFramework( targetFramework );
                String arch = platform.getArchitecture();
                CommandFilter filter = plugin.getCommandFilter();
                platformCapability.setCoreAssemblies( coreAssemblies );

                List<String> includes = ( filter != null ) ? filter.getIncludes() : new ArrayList<String>();
                List<String> excludes = ( filter != null ) ? filter.getExcludes() : new ArrayList<String>();
                platformCapability.setCommandCapability(
                    CommandCapability.Factory.createDefaultCommandCapability( includes, excludes ) );
                if ( arch != null )
                {
                    platformCapability.setArchitecture( arch );
                }
                platformCapabilities.add( platformCapability );
            }
        }
        return platformCapabilities;
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
