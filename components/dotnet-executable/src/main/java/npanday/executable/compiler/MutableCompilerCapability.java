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

import npanday.executable.MutableExecutableCapability;

import java.io.File;
import java.util.List;

/**
 * Holds the configured compiler capability.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
// TODO: Refactor to be based on the configured plugins
public class MutableCompilerCapability
    extends MutableExecutableCapability
    implements CompilerCapability
{
    private String language;

    private boolean hasJustInTime;

    private List<String> coreAssemblies;

    private File assemblyPath;

    private String targetFramework;

    public File getAssemblyPath()
    {
        return assemblyPath;
    }

    public void setAssemblyPath( File assemblyPath )
    {
        this.assemblyPath = assemblyPath;
    }

    public List<String> getCoreAssemblies()
    {
        return coreAssemblies;
    }

    public void setCoreAssemblies( List<String> coreAssemblies )
    {
        this.coreAssemblies = coreAssemblies;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage( String language )
    {
        this.language = language;
    }

    public String getTargetFramework()
    {
        return targetFramework;
    }

    public void setTargetFramework( String targetFramework )
    {
        this.targetFramework = targetFramework;
    }

    public boolean isHasJustInTime()
    {
        return hasJustInTime;
    }

    public void setHasJustInTime( boolean hasJustInTime )
    {
        this.hasJustInTime = hasJustInTime;
    }

    @Override
    public String toString()
    {
        return "CompilerCapability [" + "vendorInfo=" + vendorInfo + ", operatingSystem='" + operatingSystem
            + '\'' + ", language='" + language + '\'' + ']';
    }
}
