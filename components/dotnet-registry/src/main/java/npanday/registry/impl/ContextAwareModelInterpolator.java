package npanday.registry.impl;

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

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.PrefixAwareRecursionInterceptor;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;
import org.codehaus.plexus.interpolation.PrefixedPropertiesValueSource;
import org.codehaus.plexus.interpolation.RecursionInterceptor;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.interpolation.object.FieldBasedObjectInterpolator;
import org.codehaus.plexus.interpolation.object.ObjectInterpolationWarning;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Interpolates Maven Project expressions and registry lookups.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component role="npanday.registry.ModelInterpolator"
 */
public class ContextAwareModelInterpolator
    extends AbstractLogEnabled
    implements npanday.registry.ModelInterpolator
{
    private static final Properties ENVIRONMENT_VARIABLES;

    public static final List<String> PROJECT_PREFIXES;

    public static final List<String> PROJECT_PROPERTIES_PREFIXES;

    public static final String SETTINGS_PREFIX = "settings.";

    static
    {
        final List<String> projectPrefixes = new ArrayList<String>();
        projectPrefixes.add( "pom." );
        projectPrefixes.add( "project." );

        PROJECT_PREFIXES = Collections.unmodifiableList( projectPrefixes );

        final List<String> projectPropertiesPrefixes = new ArrayList<String>();

        projectPropertiesPrefixes.add( "pom.properties." );
        projectPropertiesPrefixes.add( "project.properties." );

        PROJECT_PROPERTIES_PREFIXES = Collections.unmodifiableList( projectPropertiesPrefixes );

        Properties environmentVariables;
        try
        {
            environmentVariables = CommandLineUtils.getSystemEnvVars( false );
        }
        catch ( final IOException e )
        {
            environmentVariables = new Properties();
        }

        ENVIRONMENT_VARIABLES = environmentVariables;
    }

    public <T> T interpolate( final T model, final MavenProject project ) throws InterpolationException
    {
        @SuppressWarnings( "unchecked" )
        final Set<String> blacklistFields = new HashSet<String>(
            FieldBasedObjectInterpolator.DEFAULT_BLACKLISTED_FIELD_NAMES
        );

        @SuppressWarnings( "unchecked" )
        final Set<String> blacklistPkgs = FieldBasedObjectInterpolator.DEFAULT_BLACKLISTED_PACKAGE_PREFIXES;

        final FieldBasedObjectInterpolator objectInterpolator = new FieldBasedObjectInterpolator(
            blacklistFields, blacklistPkgs
        );
        final Interpolator interpolator = buildInterpolator( project );

        // TODO: Will this adequately detect cycles between prefixed property references and prefixed project
        // references??
        final RecursionInterceptor interceptor = new PrefixAwareRecursionInterceptor( PROJECT_PREFIXES, true );

        try
        {
            objectInterpolator.interpolate( model, interpolator, interceptor );
        }
        finally
        {
            interpolator.clearAnswers();
        }

        if ( objectInterpolator.hasWarnings() && getLogger().isDebugEnabled() )
        {
            final StringBuffer sb = new StringBuffer();

            sb.append( "NPANDAY-116-000: One or more minor errors occurred while interpolating the model: \n" );

            @SuppressWarnings( "unchecked" )
            final List<ObjectInterpolationWarning> warnings = objectInterpolator.getWarnings();
            for ( final Iterator<ObjectInterpolationWarning> it = warnings.iterator(); it.hasNext(); )
            {
                final ObjectInterpolationWarning warning = it.next();

                sb.append( '\n' ).append( warning );
            }

            sb.append( "\n\nThese values were SKIPPED, but the assembly process will continue.\n" );

            getLogger().debug( sb.toString() );
        }

        return model;
    }

    public static Interpolator buildInterpolator( final MavenProject project )
    {
        final StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.setCacheAnswers( true );

        if ( project != null )
        {
            interpolator.addValueSource(
                new PrefixedPropertiesValueSource(
                    PROJECT_PROPERTIES_PREFIXES, project.getProperties(), true
                )
            );

            interpolator.addValueSource(
                new PrefixedObjectValueSource(
                    PROJECT_PREFIXES, project, true
                )
            );
        }

        /*final Properties settingsProperties = new Properties();
        if ( session != null && session.getSettings() != null )
        {
            settingsProperties.setProperty( "localRepository", session.getSettings().getLocalRepository() );
            settingsProperties.setProperty( "settings.localRepository", configSource.getLocalRepository().getBasedir
            () );
        }
        interpolator.addValueSource( new PropertiesBasedValueSource( settingsProperties ) );

        Properties commandLineProperties = System.getProperties();
        if ( session != null )
        {
            commandLineProperties = new Properties();
            if ( session.getExecutionProperties() != null )
            {
                commandLineProperties.putAll( session.getExecutionProperties() );
            }
            
            if ( session.getUserProperties() != null )
            {
                commandLineProperties.putAll( session.getUserProperties() );
            }
        }
        interpolator.addValueSource( new PropertiesBasedValueSource( commandLineProperties ) );
        */

        interpolator.addValueSource(
            new PrefixedPropertiesValueSource(
                Collections.singletonList( "env." ), ENVIRONMENT_VARIABLES, true
            )
        );
        interpolator.addValueSource( new WindowsRegistryValueSource( new WinRegistry() ) );

        return interpolator;
    }
}
