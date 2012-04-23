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

/**
 * The result of an execution.
 *
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class ExecutionResult
{
    private final int result;

    private final String standardOut;

    private final String standardError;

    public ExecutionResult( int result, String standardOut, String standardError )
    {
        this.result = result;
        this.standardOut = standardOut;
        this.standardError = standardError;
    }

    public int getResult()
    {
        return result;
    }

    public String getStandardOut()
    {
        return standardOut;
    }

    public String getStandardError()
    {
        return standardError;
    }
}
