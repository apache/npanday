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
package org.apache.maven.dotnet.vendor;

import org.apache.maven.dotnet.vendor.VendorInfo;
import org.apache.maven.dotnet.PlatformUnsupportedException;

import java.util.List;
import java.util.Set;
import java.io.File;

/**
 * Provides services for matching and obtaining vendor info from the npanday-settings config file.
 *
 * @author Shane Isbell
 */
public interface VendorInfoRepository
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = VendorInfoRepository.class.getName();

    /**
     * Returns a list of all vendor infos in the repository.
     *
     * @return a list of all vendor infos in the repository
     */
    List<VendorInfo> getVendorInfos();

    /**
     * Returns a list of vendor infos for the specified vendor name, vendor version and framework version.
     *
     * @param vendorName       the vendor name (Microsoft, Mono, DotGNU) to match. If this value is null,
     *                         this method will match all vendor names.
     * @param vendorVersion    the vendor version to match. If this value is null, this method will match all vendor
     *                         versions.
     * @param frameworkVersion the framework version to match. If this value is null, this method will match all framework
     *                         versions.
     * @param isDefault        if true, this method will filter out vendor info entries that do not have a default field
     * @return a list of vendor infos for the specified vendor name, vendor version and framework version
     */
    List<VendorInfo> getVendorInfosFor( String vendorName, String vendorVersion, String frameworkVersion,
                                        boolean isDefault );

    /**
     * Returns a list of vendor infos for the specified vendor info. This is a convenience method for the
     * <code>getVendorInfosFor(String, String, String, boolean)</code>. This method allows the use
     * of a vendor info parameter directly.
     *
     * @param vendorInfo the vendor info to match. This value may be incomplete, in which case, this method will return
     *                   all possible vendor infos that could match.
     * @param isDefault  if true, this method will filter out vendor info entries that do not have a default field
     * @return a list of vendor infos for the specified vendorInfo
     */
    List<VendorInfo> getVendorInfosFor( VendorInfo vendorInfo, boolean isDefault );

    /**
     * Returns the maximum version of the given set of versions.
     *
     * @param versions a set of versions from which to choose the maximum version
     * @return the maximum version from the specified set of versions.
     * @throws InvalidVersionFormatException if the format of one or more of the versions is invalid
     */
    String getMaxVersion( Set<String> versions )
        throws InvalidVersionFormatException;

    /**
     * Returns file pointing to the .NET framework installation root used for compiling artifacts.
     *
     * @param vendorInfo the vendor info
     * @return file pointing to the .NET framework installation root used for compiling artifacts
     * @throws org.apache.maven.dotnet.PlatformUnsupportedException
     *
     */
    File getInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException;

    /**
     * Returns file pointing to the .NET SDK installation root used for compiling artifacts.
     *
     * @param vendorInfo the vendor info
     * @return file pointing to the .NET SDK installation root used for compiling artifacts
     * @throws PlatformUnsupportedException
     */
    File getSdkInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException;

    /**
     * Returns true if this repository exists (and can be used), otherwise returns false.
     *
     * @return true if this repository exists (and can be used), otherwise returns false
     */
    boolean exists();

    /**
     *
     * 
     * @param vendor
     * @param frameworkVersion
     * @param artifactType
     * @return
     * @throws PlatformUnsupportedException
     */
    File getGlobalAssemblyCacheDirectoryFor( Vendor vendor, String frameworkVersion, String artifactType )
        throws PlatformUnsupportedException;

}
