/* Copyright 2010 NPanday
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package npanday.lifecycle

import npanday.lifecycle.LifecycleMapping;
import npanday.lifecycle.LifecycleMap;
import npanday.ArtifactType;
import org.junit.Test 

/**
 * A lifecycle map for testing purposes.
 * 
 * @author <a href="mailto:me@lcorneliussen.de">Lars Corneliussen</a>
 */
class TestLifecycleMap extends LifecycleMap
{
	void defineMappings(String npandayVersion) {
	    add(new LifecycleMapping(type: ArtifactType.DOTNET_LIBRARY, phases: null))
	}
	
	@Test void pseudo(){}
}
