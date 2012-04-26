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

package npanday.plugin.libraryimporter.resolve;

import npanday.executable.ExecutableRequirement;
import npanday.nuget.NugetInvoker;
import npanday.plugin.libraryimporter.skeletons.AbstractLibraryImportsProvidingMojo;


/**
 * Abstract Mojo for interaction with CSPack
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class AbstractManifestInfoMojo
    extends AbstractLibraryImportsProvidingMojo
{
    /**
     * @component
     */
    protected NugetInvoker nugetInvoker;

    /**
     * The executable identifier used to locate the right configurations from executable-plugins.xml. Can't be changed.
     */
    private String executableIdentifier = "MANIFESTINFO";

    /**
     * The configured executable version, from executable-plugins.xml, to be used. Should align to a installed
     * Azure SDK version.
     *
     * @parameter expression="${nuget.version}" default-value="1.0"
     */
    private String executableVersion;

    /**
     * The configured executable profile, from executable-plugins.xml, to be used.
     *
     * @parameter expression="${nuget.profile}"
     */
    private String executableProfile;

    protected ExecutableRequirement getExecutableRequirement()
    {
        // TODO: profile is actually an identifier; the real profile has yet to be supported
        return new ExecutableRequirement( getVendorRequirement(), executableIdentifier, executableVersion );
    }
}
