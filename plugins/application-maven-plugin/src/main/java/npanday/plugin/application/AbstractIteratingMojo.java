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

package npanday.plugin.application;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.List;

/**
 * Just abstracting the technical part of iterating inside the Mojo.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class AbstractIteratingMojo<T>
    extends AbstractMojo
{
    protected void executeItems() throws MojoFailureException, MojoExecutionException
    {
        final List<T> iterationItems = prepareIterationItems();

        getLog().info( "NPANDAY-128-003: Configured execution items " + iterationItems );

        for ( T iterationItem : iterationItems )
        {
            getLog().info( "NPANDAY-128-004: Executing iteration item " + iterationItem );

            executeItem( iterationItem );
        }
    }

    protected abstract void executeItem( T iterationItem ) throws MojoFailureException, MojoExecutionException;

    protected abstract List<T> prepareIterationItems() throws MojoFailureException, MojoExecutionException;
}


