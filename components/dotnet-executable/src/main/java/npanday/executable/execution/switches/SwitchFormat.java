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

package npanday.executable.execution.switches;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwitchFormat
{
    char leadChar;

    char valueOperatorChar;

    String switchNamePattern;

    Pattern switchPattern;

    public SwitchFormat( char leadChar, char valueOperatorChar )
    {
        this( leadChar, "\\w+", valueOperatorChar );
    }

    public SwitchFormat( char leadChar, String switchNamePattern, char valueOperatorChar )
    {
        this.leadChar = leadChar;
        this.valueOperatorChar = valueOperatorChar;
        this.switchNamePattern = switchNamePattern;

        this.switchPattern = Pattern.compile(
            Pattern.quote( String.valueOf( leadChar ) ) + "(" + switchNamePattern + ")" + Pattern.quote(
                String.valueOf( valueOperatorChar )
            ) + "(.+)"
        );
    }

    public boolean isMatchingSwitch( String argument )
    {
        // TODO: is performance relevant here?
        return switchPattern.matcher( argument ).matches();
    }

    public Switch parse( String argument ){
        final Matcher matcher = switchPattern.matcher( argument );
        if (!matcher.matches()){
            throw new IllegalArgumentException( "NPANDAY-134-000: Argument '" + argument + "' is not of format " + toString() );
        }

        // first group will always be the switch name, last always the (.*) for the value
        return new Switch(this, matcher.group(1), matcher.group(matcher.groupCount()));
    }

    public String generate(String switchName, String switchValue){
        return leadChar + switchName + valueOperatorChar + switchValue;
    }

    @Override
    public String toString()
    {
        return "SwitchFormat{leadChar=" + leadChar + ", valueOperatorChar=" + valueOperatorChar
            + ", switchNamePattern=/" + switchNamePattern + "/}";
    }

    static Splitter SPLIT_ON_SEMICOLON = Splitter.on( ';' );
    public static SwitchFormat fromStringDefinition( String formatDefinition )
    {
        String[] args =  Iterables.toArray(SPLIT_ON_SEMICOLON.split( formatDefinition ), String.class);
        return new SwitchFormat( args[0].charAt( 0 ), args[1], args[2].charAt( 0 ) );
    }
}

