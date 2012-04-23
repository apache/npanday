package npanday.executable;

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

import npanday.vendor.Vendor;
import npanday.vendor.VendorRequirement;

/**
 * Requirements that the executable plugin must satisfy to be used in the build.
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @see ExecutableCapability
 * @see CapabilityMatcher
 */
public class ExecutableRequirement
{
    private VendorRequirement vendor;

    private String profile;

    private String executableVersion;

    public ExecutableRequirement( String vendorName, String vendorVersion, String frameworkVersion, String profile )
    {
        this( vendorName, vendorVersion, frameworkVersion, profile, null );
    }

    public ExecutableRequirement(
        String vendorName, String vendorVersion, String frameworkVersion, String profile, String executableVersion )
    {
        this(new VendorRequirement( vendorName, vendorVersion, frameworkVersion ),  profile, executableVersion);
    }

    public ExecutableRequirement( Vendor vendor, String vendorVersion, String frameworkVersion, String profile )
    {
        this( vendor, vendorVersion, frameworkVersion, profile, null );
    }

    public ExecutableRequirement(
        Vendor vendor, String vendorVersion, String frameworkVersion, String profile, String executableVersion )
    {
        this(new VendorRequirement( vendor, vendorVersion, frameworkVersion ),  profile, executableVersion);
    }

    public ExecutableRequirement( VendorRequirement vendor, String profile, String executableVersion )
    {
        this.vendor = vendor;
        this.profile = profile;
        this.executableVersion = executableVersion;
    }

    public ExecutableRequirement( VendorRequirement vendor, String profile )
    {
        this(vendor, profile, null);
    }

    public VendorRequirement getVendorRequirement(){
        return vendor;
    }

    public String getProfile()
    {
        return profile;
    }

    public String getExecutableVersion()
    {
        return executableVersion;
    }
}
