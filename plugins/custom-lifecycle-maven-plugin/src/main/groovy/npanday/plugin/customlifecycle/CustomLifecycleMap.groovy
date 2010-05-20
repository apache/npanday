/* 
 * Copyright 2010 NPanday
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package npanday.plugin.customlifecycle;

import org.apache.maven.lifecycle.Lifecycle;
import npanday.lifecycle.LifecycleMapping;
import npanday.lifecycle.LifecycleStep;
import npanday.lifecycle.LifecycleMap;
import npanday.ArtifactType;
/**
 * The mapping of all compilable types to an almost empty lifecycle. It only 
 * executes the default install and deploy plugins in the corresponding phases.
 * 
 * @author Lars Corneliussen
 */
class CustomLifecycleMap extends LifecycleMap
{
	void defineMappings() {
	    def steps = [
			new LifecycleStep(
    			phase: 'install',
    			goals: ['org.apache.maven.plugins:maven-install-plugin:install']
			),
			new LifecycleStep(
    			phase: 'deploy',
    			goals: ['org.apache.maven.plugins:maven-deploy-plugin:deploy']
			)
		]
		
		
		ArtifactType.values()
    		.findAll{ArtifactType type -> type != npanday.ArtifactType.NULL && type.targetCompileType != null}
    		.each{
				ArtifactType type ->
				    add(new LifecycleMapping(type: type, steps: steps))
			}
	}
}
