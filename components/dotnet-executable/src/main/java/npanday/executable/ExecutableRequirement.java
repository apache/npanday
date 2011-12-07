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
package npanday.executable;

import npanday.vendor.Vendor;
import npanday.vendor.VendorFactory;
import npanday.vendor.VendorRequirement;

/**
 * Requirements that the executable plugin must satisfy to be used in the build.
 *
 * @author Shane Isbell
 * @see ExecutableCapability
 * @see CapabilityMatcher
 */
public interface ExecutableRequirement
{
    /**
     * Returns required framework version under which the executable runs.
     *
     * @return required framework version under which the executable runs
     */
    String getFrameworkVersion();

    /**
     * Sets required framework version.
     *
     * @param frameworkVersion
     */
    void setFrameworkVersion( String frameworkVersion );

    /**
     * Returns required profile of the executable.
     *
     * @return required profile of the executable
     */
    String getProfile();

    /**
     * Sets required profile of the executable
     *
     * @param profile
     */
    void setProfile( String profile );

    /**
     * Returns required vendor of executable
     *
     * @return required vendor of executable
     */
    Vendor getVendor();

    /**
     * Sets required vendor of executable
     *
     * @param vendor
     */
    void setVendor( Vendor vendor );

    /**
     * Sets the vendor through its name.
     */
    void setVendor( String vendorName );

    void setVendorVersion( String vendorVersion );

    String getVendorVersion();

    /**
     * Copies the relevant properties to a new VendorRequirement
     */
    VendorRequirement toVendorRequirement();

    /**
     * Provides factory services for creating a default instance of the executable requirement.
     */
    public static class Factory
    {

        /**
         * Default constructor
         */
        private Factory()
        {
        }

        /**
         * Creates a default implementation of an executable requirements.
         *
         * @return a default implementation of an executable requirements
         */
        public static ExecutableRequirement createDefaultExecutableRequirement()
        {
            return new ExecutableRequirement()
            {
                private String frameworkVersion;

                private Vendor vendor;

                private String profile;

                private String vendorVersion;

                public String getVendorVersion()
                {
                    return vendorVersion;
                }

                public VendorRequirement toVendorRequirement()
                {
                    return new VendorRequirement(vendor, vendorVersion, frameworkVersion);

                }

                public void setVendorVersion( String vendorVersion )
                {
                    this.vendorVersion = vendorVersion;
                }

                public String getFrameworkVersion()
                {
                    return frameworkVersion;
                }

                public void setFrameworkVersion( String frameworkVersion )
                {
                    this.frameworkVersion = frameworkVersion;
                }

                public Vendor getVendor()
                {
                    return vendor;
                }

                public void setVendor( Vendor vendor )
                {
                    this.vendor = vendor;
                }

                public void setVendor( String vendorName )
                {
                    setVendor( VendorFactory.createVendorFromName( vendorName ) );
                }

                public String getProfile()
                {
                    return profile;
                }

                public void setProfile( String profile )
                {
                    this.profile = profile;
                }
            };

        }
    }
}
