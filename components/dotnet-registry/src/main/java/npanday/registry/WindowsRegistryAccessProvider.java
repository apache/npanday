package npanday.registry;

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

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public interface WindowsRegistryAccessProvider
{
   /**
     * Tries to get a registry value from the Windows registry.
     * @param registryHKey
     * @param key
     * @param valueName
     * @return The value of the key or null.
     */
    String getValue( RegistryHKey registryHKey, String key, String valueName ) throws
        WindowsRegistryAccessException;

    public static enum RegistryHKey
    {
        HKLM( "HKEY_LOCAL_MACHINE", 0x80000002 ),
        HKCU( "HKEY_CURRENT_USER",  0x80000001 );

        private String longName;
        private int hkey;

        RegistryHKey( String longName, int hkey )
        {
            this.longName = longName;
            this.hkey = hkey;
        }

        public String getLongName()
        {
            return longName;
        }

        public int getHKey()
        {
            return hkey;
        }

        public static RegistryHKey tryGetFromName(String name){
            checkArgument( name != null, "Name must not be null");

            if (name.equals("HKLM") || name.equals(HKLM.getLongName()))
                return HKLM;
            if (name.equals("HKCU") || name.equals(HKCU.getLongName()))
                return HKCU;

            return null;
        }
    }
}

