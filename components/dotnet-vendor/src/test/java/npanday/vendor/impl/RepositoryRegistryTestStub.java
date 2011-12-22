package npanday.vendor.impl;

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

import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

final class RepositoryRegistryTestStub
    implements RepositoryRegistry
{
    private SettingsRepository settingsRepository;

    public boolean isEmpty()
    {
        return false;
    }

    public synchronized void loadFromInputStream( InputStream inputStream )
        throws IOException, NPandayRepositoryException
    {
    }

    public synchronized void loadFromFile( String fileName )
        throws IOException, NPandayRepositoryException
    {
    }

    public synchronized void loadFromResource( String fileName, Class sourceClass )
        throws IOException, NPandayRepositoryException
    {
    }

    public synchronized void addRepository( String name, Repository repository )
    {
    }

    public synchronized Repository find( String name )
    {
        return settingsRepository;
    }

    public synchronized void removeRepository( String name )
    {
    }

    public synchronized Set getRepositoryNames()
    {
        return null;
    }

    public synchronized void empty()
    {
    }

    void setSettingRepository(SettingsRepository settingsRepository)
    {
        this.settingsRepository = settingsRepository;
    }
}
