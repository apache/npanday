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

package npanday.packaging;

import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugin.assembly.io.AssemblyReadException;
import org.apache.maven.plugin.assembly.io.DefaultAssemblyReader;
import org.apache.maven.plugin.assembly.model.Assembly;

import java.io.File;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 *
 * @plexus.component role="npanday.packaging.MixinAsssemblyReader"
 */
public class MixinAsssemblyReader extends DefaultAssemblyReader
{
    private String[] componentDescriptors;

    /**
     * Add the contents of all included components to main assembly
     *
     * @throws org.apache.maven.plugin.assembly.io.AssemblyReadException
     *
     * @throws org.apache.maven.plugin.MojoFailureException
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    @Override
    protected void mergeComponentsWithMainAssembly( Assembly assembly, File assemblyDir,
                                                    AssemblerConfigurationSource configSource )
        throws AssemblyReadException
    {
        appendComponentDescriptors( assembly );

        super.mergeComponentsWithMainAssembly( assembly, assemblyDir,
                                               configSource );    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Add component descriptors from other sources.
     */
    protected void appendComponentDescriptors( Assembly assembly )
    {
        for (String componentDescriptor : componentDescriptors){
            getLogger().debug( "NPANDAY-110-001: Mixing in component descriptor '" + componentDescriptor + "' to assembly with id '" + assembly.getId() + "'.");
            assembly.addComponentDescriptor( componentDescriptor );
        }
    }

    public void setComponentDescriptors( String[] componentDescriptors )
    {
        this.componentDescriptors = componentDescriptors;
    }
}
