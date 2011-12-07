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
package npanday.executable.compiler;

import npanday.vendor.Vendor;
import npanday.executable.ExecutableRequirement;
import npanday.vendor.VendorFactory;
import npanday.vendor.VendorRequirement;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Requirements that the compiler plugin must satisfy to be used in the build.
 *
 * @author Shane Isbell
 * @see CompilerCapability
 */
public interface CompilerRequirement
    extends ExecutableRequirement
{

    /**
     * Returns the required language for the compiler
     *
     * @return the required language for the compiler
     */
    String getLanguage();

    /**
     * Sets required language for the compiler
     *
     * @param language the required language for the compiler
     */
    void setLanguage( String language );

    public static class Factory
    {

        private Factory()
        {
        }

        public static CompilerRequirement createDefaultCompilerRequirement()
        {
            return new CompilerRequirement()
            {
                private String language;

                private String frameworkVersion;

                private Vendor vendor;

                private String profile;

                private String vendorVersion;

                public String getVendorVersion()
                {
                    return vendorVersion;
                }

                /**
                 * Copies the relevant properties to a new VendorRequirement
                 */
                public VendorRequirement toVendorRequirement()
                {
                    return new VendorRequirement( vendor, vendorVersion, frameworkVersion );
                }

                public void setVendorVersion( String vendorVersion )
                {
                    this.vendorVersion = vendorVersion;
                }

                public String getLanguage()
                {
                    return language;
                }

                public void setLanguage( String language )
                {
                    this.language = language;
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
                    if ( !isNullOrEmpty( vendorName ) )
                    {
                        setVendor( VendorFactory.createVendorFromName( vendorName ) );
                    }
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
