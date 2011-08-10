/* 
 * Copyright 2010 NPanday
 * 
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
package npanday.plugin.customlifecycle;

import npanday.lifecycle.LifecyclePrinter;

import org.codehaus.gmaven.mojo.GroovyMojo
import org.apache.maven.project.MavenProject;
/**
 * @author Lars Corneliussen
 * @goal describe
 * @description Lists the types and lifecycles this plugin configures when used as an extension.
 */
class DescribeMojo
    extends GroovyMojo
{
     /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

	void execute() {
		LifecyclePrinter.printAll(CustomLifecycleMap, project.version, {getLog().info(it)})
	}
}
