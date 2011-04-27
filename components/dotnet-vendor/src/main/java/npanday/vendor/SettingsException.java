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

/**
 *   Exception class thrown when there is trouble reading the NPanday-Settings.
 *   @author Lars Corneliussen (me@lcorneliussen.de)
 */
public class SettingsException
    extends Throwable
{
    public SettingsException()
    {
    }

    public SettingsException( String message )
    {
        super( message );
    }

    public SettingsException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public SettingsException( Throwable cause )
    {
        super( cause );
    }
}
