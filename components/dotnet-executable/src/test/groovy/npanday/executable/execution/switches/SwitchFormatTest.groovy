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

package npanday.executable.execution.switches

import org.junit.Test

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
class SwitchFormatTest
{

    @Test
    void testSimpleSwitch()
    {
        def format = new SwitchFormat((char) '-', (char) ':')
        assert format.isMatchingSwitch("-x:y") == true
        def sw = format.parse("-x:y");
        assert sw.name == "x"
        assert sw.value == "y"
    }

    @Test
    void testSimpleSwitch_negative()
    {
        def format = new SwitchFormat((char) '-', (char) ':')
        assert format.isMatchingSwitch("/x:y") == false
        assert format.isMatchingSwitch("-x=y") == false
        assert format.isMatchingSwitch("-x=z:y") == false

    }

    @Test
    void testMsDeployStyleSwitch()
    {
        def format = new SwitchFormat((char) '-', "\\w+(\\:\\w+)", (char) '=')
        assert format.isMatchingSwitch("-x:y=z") == true
        def sw = format.parse("-x:y=z");
        assert sw.name == "x:y"
        assert sw.value == "z"
    }

    @Test
    void testParseFromDefinition()
    {
        def parsed = SwitchFormat.fromStringDefinition("-;\\w+(\\:\\w+);=").toString()
        def handCrafted = new SwitchFormat((char) '-', "\\w+(\\:\\w+)", (char) '=').toString()
        assert parsed == handCrafted
    }
}
