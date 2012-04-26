import org.junit.Test
import npanday.nuget.NugetVersionSpec
import npanday.nuget.NugetSemanticVersion
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
class NugetVersionSpecTest {

    @Test
    void parse_simple(){
        NugetVersionSpec spec = NugetVersionSpec.parse("1.0-a")
        assert spec.IsMinInclusive
        assert spec.MinVersion
        assert !spec.IsMaxInclusive
        assert spec.MaxVersion == null
    }

    @Test
    void parse_exact(){
        NugetVersionSpec spec = NugetVersionSpec.parse("[1.0-a]")
        assert spec.IsMinInclusive
        assert spec.MinVersion == NugetSemanticVersion.parse("1.0-a")
        assert spec.IsMaxInclusive
        assert spec.MaxVersion == NugetSemanticVersion.parse("1.0-a")
    }

    @Test
    void parse_between_exclusive(){
        NugetVersionSpec spec = NugetVersionSpec.parse("(1.0, 2.0)")
        assert !spec.IsMinInclusive
        assert spec.MinVersion == NugetSemanticVersion.parse("1.0")
        assert !spec.IsMaxInclusive
        assert spec.MaxVersion == NugetSemanticVersion.parse("2.0")
    }

    @Test
    void isSatisfiedBy_simple(){
         assert NugetVersionSpec.parse("1.0").isSatisfiedBy( NugetSemanticVersion.parse("1.0"))
         assert NugetVersionSpec.parse("1.0").isSatisfiedBy( NugetSemanticVersion.parse("1.1"))
    }

    @Test
    void isSatisfiedBy_exact(){
        def spec = NugetVersionSpec.parse( "[1.0]" )
        assert spec.isSatisfiedBy( NugetSemanticVersion.parse("1.0"))
        assert !spec.isSatisfiedBy( NugetSemanticVersion.parse("1.0.1"))
        assert !spec.isSatisfiedBy( NugetSemanticVersion.parse("1.1"))
        assert !spec.isSatisfiedBy( NugetSemanticVersion.parse("0.9"))
    }

    @Test
    void isSatisfiedBy_between_exclusive(){
        def spec = NugetVersionSpec.parse( "(1.0, 2.0)" )
        assert !spec.isSatisfiedBy( NugetSemanticVersion.parse("1.0"))
        assert spec.isSatisfiedBy( NugetSemanticVersion.parse("1.1"))
        assert spec.isSatisfiedBy( NugetSemanticVersion.parse("1.0.1"))
        assert spec.isSatisfiedBy( NugetSemanticVersion.parse("1.9"))
        assert spec.isSatisfiedBy( NugetSemanticVersion.parse("1.9.9.9"))
        assert !spec.isSatisfiedBy( NugetSemanticVersion.parse("2.0"))
    }

    @Test
    void isSatisfiedBy_with_special(){
        def spec = NugetVersionSpec.parse( "2.0-beta" )
        assert !spec.isSatisfiedBy( NugetSemanticVersion.parse("2.0-alpha"))
        assert spec.isSatisfiedBy( NugetSemanticVersion.parse("2.0-beta"))
        assert spec.isSatisfiedBy( NugetSemanticVersion.parse("2.0-beta2"))
        assert spec.isSatisfiedBy( NugetSemanticVersion.parse("2.0"))
    }
}
