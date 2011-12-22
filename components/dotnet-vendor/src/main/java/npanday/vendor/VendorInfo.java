package npanday.vendor;

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

import npanday.PlatformUnsupportedException;

import java.io.File;
import java.util.List;

/**
 * Provides accessors for obtaining information about a vendor. 
 *
 * @author Shane Isbell
 */
public interface VendorInfo
{
    /**
     * Returns the vendor, for example {@link Vendor#MONO} or {@link Vendor#MICROSOFT}
     */
    Vendor getVendor();

    /**
     * Returns vendor version, in order to define or determine which tools to use
     * exactly. This is corresponding to 'tools version' in MSBuild or the CSharp compiler.
     *
     * Note that you could use a newer vendor to target an older framework version.
     */
    String getVendorVersion();

    /**
     * Returns the framework version, which actually is the version of the .NET framework
     * to compile against. This will also be the default CLR version, the compiled results
     * would run on by default.
     *
     * @return the framework version of the executable
     */
    String getFrameworkVersion();

    /**
     * Returns all paths where executable for the specified vendor and version are located. Given a particular
     * exectuable, NPanday will scan the paths and find the first matching one.
     *
     * @return the path where the executable lives
     */
    List<File> getExecutablePaths();

    /**
     * If the vendor information is the default (or preferred) value for a given vendor, returns true,
     * otherwise returns false. This allows the vendor matching framework to choose a specific version of the
     * compiler vendor (such as MONO 1.1.13.8 vs 1.1.18) if such a version is not specified within the pom.
     *
     * @return If the vendor information is the default (or preferred) value for a given vendor, returns true,
     *         otherwise returns false.
     */
    boolean isDefault();

    File getSdkInstallRoot();

    File getInstallRoot();

    File getGlobalAssemblyCacheDirectoryFor( String artifactType )
        throws PlatformUnsupportedException;

    /**
     * Provides factory services for creating a default instance of vendor info.
     */
    public static class Factory
    {
    }
}

