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

package npanday.resolver;

import com.google.common.collect.Sets;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * The instance of this cache should span the full maven reactor build.
 * Resolving the artifact file once pr. artifact id is enough.
 *
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 * @plexus.component role="npanday.resolver.NPandayResolutionCache"
 */
public class NPandayResolutionCache
        extends AbstractLogEnabled
{
    private Hashtable<String, File> cache = new Hashtable<String, File>();

    public void put(Artifact artifact){
        cache.put(artifact.getId(), artifact.getFile());
    }

    public Boolean applyTo(Artifact artifact){
        String key = artifact.getId();
        if (cache.containsKey(key)){
           File resolvedFile = cache.get(key);
           if (resolvedFile != null){
               artifact.setFile(resolvedFile);
               artifact.setResolved(true);
           }
           return true;
       }
       return false;
    }
}
