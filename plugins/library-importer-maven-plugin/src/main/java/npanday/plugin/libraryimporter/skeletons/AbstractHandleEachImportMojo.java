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

import npanday.plugin.libraryimporter.model.NugetPackage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public abstract class AbstractHandleEachImportMojo
    extends AbstractLibraryImportsProvidingMojo
{

    @Override
    protected void innerExecute() throws MojoExecutionException, MojoFailureException
    {
        super.innerExecute();

        for ( NugetPackage nuget : getNugetImports() )
        {
            getLog().debug( "NPANDAY-151-000: handling package " + nuget.toString() );
            try {
                handleNugetPackage(nuget);
            }
            catch (MojoExecutionException e){
                throw new MojoExecutionException( "NPANDAY-151-001: error handling " + nuget.toString(), e);
            }
            catch (MojoFailureException e){
                throw new MojoExecutionException( "NPANDAY-151-002: error handling " + nuget.toString(), e);
            }
            catch (Exception e){
                throw new MojoExecutionException( "NPANDAY-151-003: error handling " + nuget.toString(), e);
            }
        }
    }

    protected abstract void handleNugetPackage( NugetPackage nuget ) throws MojoExecutionException, MojoFailureException;
}
