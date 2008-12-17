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
package npanday.vendor;

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
     * Returns vendor
     *
     * @return vendor
     */
    Vendor getVendor();

    /**
     * @param vendor
     */
    void setVendor( Vendor vendor );

    /**
     * Returns vendor version.
     *
     * @return vendor version
     */
    String getVendorVersion();

    /**
     * Sets vendor version
     *
     * @param vendorVersion the vendor version
     */
    void setVendorVersion( String vendorVersion );

    /**
     * Returns framework version of the executable
     *
     * @return the framework version of the executable
     */
    String getFrameworkVersion();

    /**
     * Sets the framework version of the executable
     *
     * @param frameworkVersion
     */
    void setFrameworkVersion( String frameworkVersion );

    /**
     * Returns the path where the executable lives.
     *
     * @return the path where the executable lives
     */
    List<File> getExecutablePaths();

    /**
     * Sets the path where the executable lives.
     *
     * @param executablePaths the path where the executable lives
     */
    void setExecutablePaths( List<File> executablePaths );

    /**
     * If the vendor information is the default (or preferred) value for a given vendor, returns true,
     * otherwise returns false. This allows the vendor matching framework to choose a specific version of the
     * compiler vendor (such as MONO 1.1.13.8 vs 1.1.18) if such a version is not specified within the pom.
     *
     * @return If the vendor information is the default (or preferred) value for a given vendor, returns true,
     *         otherwise returns false.
     */
    boolean isDefault();

    /**
     * Set to true if the vendor information is the default, otherwise set to false.
     *
     * @param isDefault
     */
    void setDefault( boolean isDefault );

    /**
     * Provides factory services for creating a default instance of vendor info.
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
         * Creates a default implementation of vendor info.
         *
         * @return a default implementation of vendor info
         */
        public static VendorInfo createDefaultVendorInfo()
        {
            return new VendorInfo()
            {
                private Vendor vendor;

                private String vendorVersion;

                private String frameworkVersion;

                private List<File> executablePaths;

                private boolean isDefault;

                public boolean isDefault()
                {
                    return isDefault;
                }

                public void setDefault( boolean aDefault )
                {
                    isDefault = aDefault;
                }

                public List<File> getExecutablePaths()
                {
                    return executablePaths;
                }

                public void setExecutablePaths( List<File> executablePaths )
                {
                    this.executablePaths = executablePaths;
                }

                public Vendor getVendor()
                {
                    return vendor;
                }

                public void setVendor( Vendor vendor )
                {
                    this.vendor = vendor;
                }

                public String getVendorVersion()
                {
                    return vendorVersion;
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

                public String toString()
                {
                    return "Vendor = " + vendor + ", Vendor Version = " + vendorVersion + ", Framework Version = " +
                        frameworkVersion + ", Executable Paths = " +
                        ( ( executablePaths != null ) ? executablePaths : "" );
                }
            };
        }
    }

}
