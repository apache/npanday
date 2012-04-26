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

package npanday.plugin.libraryimporter.skeletons;

import com.google.common.base.Splitter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import static com.google.common.collect.Iterables.toArray;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class AbstractNPandayMojo
        extends AbstractMojo
{
    public Splitter SPLIT_ON_SEMICOLON = Splitter.on( ';' ).omitEmptyStrings().trimResults();

    /**
     * If the execution of this goal is to be skipped.
     *
     * @parameter
     */
    protected boolean skip;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * The maven project helper.
     *
     * @component
     */
    protected MavenProjectHelper projectHelper;

    public final void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info(
                    "NPANDAY-126-000: Execution of '" + getClass().getSimpleName() + "' was skipped by configuration."
            );
            return;
        }

        setupParameters();

        innerExecute();
    }

    protected void setupParameters()
    {

    }

    protected String[] firstNonNull( String commandlineValues, String[] configurationValues )
    {
        return firstNonNull( commandlineValues, configurationValues, new String[0] );
    }

    protected String[] firstNonNull( String commandlineValues, String[] configurationValues, String[] defaultValue )
    {
        if ( commandlineValues != null )
        {
            return toArray(
                    SPLIT_ON_SEMICOLON.split( commandlineValues ), String.class
            );
        }

        if (configurationValues != null){
            return configurationValues;
        }

        return defaultValue;
    }

    protected abstract void innerExecute() throws MojoExecutionException, MojoFailureException;
}
