package npanday.executable.compiler;

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

import npanday.executable.ExecutableRequirement;
import npanday.vendor.Vendor;

/**
 * Requirements that the compiler plugin must satisfy to be used in the build.
 *
 * @author Shane Isbell
 * @see CompilerCapability
 */
public class CompilerRequirement
    extends ExecutableRequirement
{
    String language;

    public CompilerRequirement( Vendor vendor, String vendorVersion, String frameworkVersion, String profile, String language )
    {
        super( vendor, vendorVersion, frameworkVersion, profile );
        this.language = language;
    }

    public CompilerRequirement( String vendorName, String vendorVersion, String frameworkVersion, String profile, String language )
    {
        super( vendorName, vendorVersion, frameworkVersion, profile );
        this.language = language;
    }

    /**
     * Returns the required language for the compiler
     *
     * @return the required language for the compiler
     */
    public String getLanguage(){
        return language;
    }

    /**
     * Sets required language for the compiler
     *
     * @param language the required language for the compiler
     */
    public void setLanguage( String language ){
        this.language = language;
    }
}
