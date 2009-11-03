package npanday.its;

/*
 * Copyright 2009
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class NPandayITIntraProjectDependencyTest
    extends AbstractNPandayIntegrationTestCase
{
    public NPandayITIntraProjectDependencyTest()
    {
        super( "(1.0,)" ); // 1.0.1+
    }
    
    public void testIntraProjectDependency()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/IntraProjectDependency" );
        Verifier verifier = getVerifier( testDir );
		verifier.executeGoal( "install" );
        verifier.executeGoal( "test" );
        //verifier.assertFilePresent( new File( testDir, "npanday-9903/" + getAssemblyFile( "npanday-9903", "1.0.0", "zip" ) ).getAbsolutePath() );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
    }
}
