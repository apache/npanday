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
package npanday.lifecycle;

import npanday.ArtifactType 

/**
 * Prints the a lifecycle map to a given printer closure.
 * 
 * @author <a href="mailto:me@lcorneliussen.de">Lars Corneliussen</a>
 *
 */
class LifecyclePrinter {
	
	static void printAll(Class lifecycleMap, String npandayVersion, Closure printLine) {
		def g = lifecycleMap.newInstance() as LifecycleMap
		
		new LifecyclePrinter().print(g.buildMap(npandayVersion), printLine)
	}
	
	void print(List<LifecycleMapping> lifecycleMappings, Closure printLine){
		List<ArtifactType> types = ArtifactType.values()
				.findAll{it != ArtifactType.NULL}
		
		types.each{type ->
			printLine("");
			printLine("artifact type: ${type.packagingType} (*.${type.extension})");
			
			def mapping = lifecycleMappings.find{it.type == type}
			if (mapping)
			{
				mapping.phases.eachWithIndex{phase, stepPos ->
					printLine("  on ${phase.name} executes:")
					phase.goals.eachWithIndex{goal, goalPos ->
						printLine("    - ${goal}")
					}
				}
			}
		}
	}
}
