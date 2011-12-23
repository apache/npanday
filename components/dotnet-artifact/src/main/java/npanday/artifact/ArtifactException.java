package npanday.artifact;

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

/**
 * Thrown to indicate an artifact exception.
 *
 * @author Shane Isbell
 */
public class ArtifactException
    extends Exception
{

    static final long serialVersionUID = -123887634943843L;

    /**
     * Constructs an <code>ArtifactException</code>  with no exception message.
     */
    public ArtifactException()
    {
        super();
    }

    /**
     * Constructs an <code>ArtifactException</code> with the specified exception message.
     *
     * @param message the exception message
     */
    public ArtifactException( String message )
    {
        super( message );
    }

    /**
     * Constructs an <code>ArtifactException</code> with the specified exception message and cause of the exception.
     *
     * @param message the exception message
     * @param cause   the cause of the exception
     */
    public ArtifactException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Constructs an <code>ArtifactException</code> with the cause of the exception.
     *
     * @param cause the cause of the exception
     */
    public ArtifactException( Throwable cause )
    {
        super( cause );
    }
}
