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

import java.util.List;

/**
 * User-defined configuration information for an executable or compiler.
 *
 * @author Shane Isbell
 * @see RepositoryExecutableContext
 */
public class ExecutableConfig
{
    private List<String> commands;

    private List<String> executionPaths;

    /**
     * The commands to pass to the executable plugin.
     *
     * @return the commands to pass to the executable plugin
     */
    public List<String> getCommands()
    {
        return commands;
    }

    /**
     * Sets commands to pass to the executable plugin.
     *
     * @param commands the user-defined commands to pass to the executable plugin
     */
    public void setCommands( List<String> commands )
    {
        this.commands = commands;
    }

    /**
     * The execution path of the executable. This can be an absolute path to the executable or just the name of the
     * executable as invoked from the command line.
     *
     * @return the execution path of the executable
     */
    public List<String> getExecutionPaths()
    {
        return executionPaths;
    }

    /**
     * Sets the executation path of the executable.
     *
     * @param executionPaths the execution paths
     */
    public void setExecutionPaths( List<String> executionPaths )
    {
        this.executionPaths = executionPaths;
    }
}
