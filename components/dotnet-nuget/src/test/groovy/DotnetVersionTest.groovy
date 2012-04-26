import npanday.nuget.DotnetVersion
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
class DotnetVersionTest {
    @Test
    void parse_1_0()
    {
        DotnetVersion v = DotnetVersion.parse("1.0")
        assert v.getMajor() == 1
        assert v.getMinor() == 0
        assert v.getPatch() == 0
        assert v.getBuild() == 0
    }

    @Test
    void parse_1_2_3_4()
    {
        DotnetVersion v = DotnetVersion.parse("1.2.3.4")
        assert v.getMajor() == 1
        assert v.getMinor() == 2
        assert v.getPatch() == 3
        assert v.getBuild() == 4
    }

    @Test
    void compare()
    {
        assert DotnetVersion.parse("1.0") < DotnetVersion.parse("1.1")
        assert DotnetVersion.parse("1.1.0") < DotnetVersion.parse("1.1.1")
        assert DotnetVersion.parse("1.1.1.0") < DotnetVersion.parse("1.1.1.1")

        assert DotnetVersion.parse("1.1") > DotnetVersion.parse("1.0")
        assert DotnetVersion.parse("1.1.1") > DotnetVersion.parse("1.1.0")
        assert DotnetVersion.parse("1.1.1.1") > DotnetVersion.parse("1.1.1.0")

        assert DotnetVersion.parse("1.1") != DotnetVersion.parse("1.0")
        assert DotnetVersion.parse("1.1.1") != DotnetVersion.parse("1.1.0")
        assert DotnetVersion.parse("1.1.1.1") != DotnetVersion.parse("1.1.1.0")

        assert DotnetVersion.parse("1.1") == DotnetVersion.parse("1.1")
        assert DotnetVersion.parse("1.1.1") == DotnetVersion.parse("1.1.1")
        assert DotnetVersion.parse("1.1.1.1") == DotnetVersion.parse("1.1.1.1")
    }
}
