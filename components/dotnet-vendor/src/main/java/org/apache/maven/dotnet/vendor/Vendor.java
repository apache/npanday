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

/**
 * Enumeration of supported vendors.
 *
 * @author Shane Isbell
 */
public enum Vendor
{
    /**Microsoft Vendor*/
    MICROSOFT( "MICROSOFT" ),

    /**Mono (or Novell) vendor*/
    MONO( "MONO" ),

    /**DotGNU Vendor*/
    DOTGNU( "DotGNU" ),

    /**NULL Vendor*/
    NULL("NULL");

    /**
     * The vendor name
     */
    private final String vendorName;

    /**
     * Constructor
     *
     * @param vendorName the vendor name
     */
    Vendor( String vendorName )
    {
        this.vendorName = vendorName;
    }

    /**
     * Returns the vendor name.
     *
     * @return the vendor name
     */
    public String getVendorName()
    {
        return vendorName;
    }
}
