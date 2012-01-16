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

package npanday.msbuild;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import npanday.vendor.VendorRequirement;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class MsbuildInvocationParameters
{
    private VendorRequirement vendor;

    private File file;

    private Map<String, String> properties = Maps.newHashMap();

    public MsbuildInvocationParameters(
        VendorRequirement vendor, File file )
    {
        Preconditions.checkArgument( vendor != null, "vendor was null!" );
        Preconditions.checkArgument( file != null, "file was null!" );

        this.vendor = vendor;
        this.file = file;
    }

    public void setProperty( String name, String value )
    {
        Preconditions.checkArgument( name != null, "name was null!" );
        Preconditions.checkArgument( value != null, "value was null!" );

        properties.put( name, value );
    }

    public VendorRequirement getVendorRequirement()
    {
        return vendor;
    }

    public Collection<String> buildCommands()
    {
        List<String> commands = Lists.newArrayList();

        // TODO: support /target, /maxcpucount, /toolsversion, /verbosity

        for ( Map.Entry<String, String> propEntry : properties.entrySet() )
        {
            commands.add( "/p:" + propEntry.getKey() + "=" + propEntry.getValue() );
        }

        commands.add( file.getAbsolutePath() );

        return commands;
    }
}
