import npanday.nuget.NugetSemanticVersion
import org.junit.Test
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, version 2.0 (the
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

/**
 * 
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
class NugetSemanticVersionTest {

    @Test
    void compare()
    {
        assert NugetSemanticVersion.parse( "1.0" ) < NugetSemanticVersion.parse("1.1")
        assert NugetSemanticVersion.parse("1.1.0") < NugetSemanticVersion.parse("1.1.1")
        assert NugetSemanticVersion.parse("1.1.1.0") < NugetSemanticVersion.parse("1.1.1.1")

        assert NugetSemanticVersion.parse("1.1") > NugetSemanticVersion.parse("1.0")
        assert NugetSemanticVersion.parse("1.1.1") > NugetSemanticVersion.parse("1.1.0")
        assert NugetSemanticVersion.parse("1.1.1.1") > NugetSemanticVersion.parse("1.1.1.0")
    }

    @Test
    void compare_special()
    {
        assert NugetSemanticVersion.parse("1.1-a") < NugetSemanticVersion.parse("1.1-b")
        assert NugetSemanticVersion.parse("1.1.1-a") < NugetSemanticVersion.parse("1.1.1-b")
        assert NugetSemanticVersion.parse("1.1.1.1-a") < NugetSemanticVersion.parse("1.1.1.1-b")

        assert NugetSemanticVersion.parse("1.1-b") > NugetSemanticVersion.parse("1.1-a")
        assert NugetSemanticVersion.parse("1.1.1-b") > NugetSemanticVersion.parse("1.1.1-a")
        assert NugetSemanticVersion.parse("1.1.1.1-b") > NugetSemanticVersion.parse("1.1.1.1-a")
    }

    @Test
    void equality(){
        assert NugetSemanticVersion.parse("1.1") != NugetSemanticVersion.parse("1.0")
        assert NugetSemanticVersion.parse("1.1.1") != NugetSemanticVersion.parse("1.1.0")
        assert NugetSemanticVersion.parse("1.1.1.1") != NugetSemanticVersion.parse("1.1.1.0")

        assert NugetSemanticVersion.parse("1.1") == NugetSemanticVersion.parse("1.1")
        assert NugetSemanticVersion.parse("1.1.1") == NugetSemanticVersion.parse("1.1.1")
        assert NugetSemanticVersion.parse("1.1.1.1") == NugetSemanticVersion.parse("1.1.1.1")
    }

    @Test
    void equality_special(){
        assert NugetSemanticVersion.parse("1.1-a") != NugetSemanticVersion.parse("1.1-b")
        assert NugetSemanticVersion.parse("1.1.1-a") != NugetSemanticVersion.parse("1.1.1-b")
        assert NugetSemanticVersion.parse("1.1.1.1-a") != NugetSemanticVersion.parse("1.1.1.1-b")

        assert NugetSemanticVersion.parse("1.1-a") == NugetSemanticVersion.parse("1.1-a")
        assert NugetSemanticVersion.parse("1.1.1-a") == NugetSemanticVersion.parse("1.1.1-a")
        assert NugetSemanticVersion.parse("1.1.1.1-a") == NugetSemanticVersion.parse("1.1.1.1-a")
    }
}
