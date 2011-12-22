package npanday.registry.impl;

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

import hidden.org.codehaus.plexus.interpolation.AbstractValueSource;
import npanday.registry.WindowsRegistryAccessException;
import npanday.registry.WindowsRegistryAccessProvider;
import org.codehaus.plexus.interpolation.ValueSource;

/**
 * Tries to find registry settings for all expressions starting with HKLM or HKCU.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class WindowsRegistryValueSource
    extends AbstractValueSource
    implements ValueSource
{
   private WindowsRegistryAccessProvider registry;

    public WindowsRegistryValueSource( WindowsRegistryAccessProvider registry )
    {
        super( true );
        this.registry = registry;
    }

    public Object getValue( String expression )
    {
        int indexOfBackslash = expression.indexOf( "\\" );
        if (indexOfBackslash == -1)
            return null;

        String hkeyExpression = expression.substring( 0, indexOfBackslash );

        final WindowsRegistryAccessProvider.RegistryHKey registryHKey = WindowsRegistryAccessProvider.RegistryHKey
            .tryGetFromName(
                hkeyExpression
            );

        if (registryHKey == null)
            return null;

        if (expression.length() < indexOfBackslash || !expression.contains( "@" ))
            return null;

        String keyAndValueNamePart = expression.substring( indexOfBackslash +1 );
        int indexOfAt = keyAndValueNamePart.indexOf( '@' );

        String key = keyAndValueNamePart.substring( 0, indexOfAt );
        String valueName = keyAndValueNamePart.substring( indexOfAt+1 );

        try
        {
            final String value = registry.getValue( registryHKey, key, valueName );
            addFeedback( "NPANDAY-118-001: Retrieved the registry value for " + expression + ": " + value);
            return value == null ? "" : value;
        }
        catch ( WindowsRegistryAccessException e )
        {
            addFeedback( "NPANDAY-118-000: Could not retrieve the registry value for " + expression, e );
            return "";
        }
    }
}

