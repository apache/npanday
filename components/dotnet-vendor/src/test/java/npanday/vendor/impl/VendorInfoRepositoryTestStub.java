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
package npanday.vendor.impl;

import npanday.vendor.VendorInfoRepository;
import npanday.vendor.VendorInfo;
import npanday.vendor.InvalidVersionFormatException;
import npanday.vendor.Vendor;

import npanday.PlatformUnsupportedException;
import npanday.vendor.VendorRequirement;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class VendorInfoRepositoryTestStub
    implements VendorInfoRepository
{

    private List<VendorInfo> vendorInfos;

    public boolean exists()
    {
        return true;
    }

    public File getGlobalAssemblyCacheDirectoryFor( Vendor vendor, String frameworkVersion, String artifactType )
        throws PlatformUnsupportedException
    {
        return null;  
    }

    /**
     * Determines, if the repository is empty. This happens, if the configuration couldn't be read, because no file was
     * available, or when the underlying SettingsRepository wasn't initialized properly.
     */
    public boolean isEmpty()
    {
        return vendorInfos != null && vendorInfos.size() > 0;
    }

    public File getInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        return null;
    }

    public File getSdkInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        return null;
    }

    public VendorInfo getSingleVendorInfoByRequirement( VendorRequirement vendorRequirement ) throws PlatformUnsupportedException {
        return null;
    }

    public String getMaxVersion( Set<String> versions )
        throws InvalidVersionFormatException
    {
        return new VersionMatcher().getMaxVersion( versions );
    }

    public List<VendorInfo> getVendorInfosFor( String vendorName, String vendorVersion, String frameworkVersion,
                                               boolean isDefault )
    {
        return new ArrayList<VendorInfo>();
    }

    public List<VendorInfo> getVendorInfosFor( VendorRequirement vendorRequirement, boolean isDefault )
    {
        return vendorInfos;
    }

    void setVendorInfos( List<VendorInfo> vendorInfos )
    {
        this.vendorInfos = vendorInfos;
    }
}
