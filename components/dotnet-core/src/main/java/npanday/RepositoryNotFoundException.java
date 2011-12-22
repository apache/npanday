package npanday;

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
 * Exception class thrown when a Repository does not exist in the registry.
 *
 * @author Shane Isbell
 */
public class RepositoryNotFoundException
    extends Exception
{

    static final long serialVersionUID = 7228089324L;

    /**
     * Constructs an <code>RepositoryNotFoundException</code>  with no exception message.
     */
    public RepositoryNotFoundException()
    {
        super();
    }

    /**
     * Constructs an <code>RepositoryNotFoundException</code> with the specified exception message.
     *
     * @param message the exception message
     */
    public RepositoryNotFoundException( String message )
    {
        super( message );
    }

    /**
     * Constructs an <code>RepositoryNotFoundException</code> with the specified exception message and cause of the exception.
     *
     * @param message the exception message
     * @param cause   the cause of the exception
     */
    public RepositoryNotFoundException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Constructs an <code>RepositoryNotFoundException/code> with the cause of the exception.
     *
     * @param cause the cause of the exception
     */
    public RepositoryNotFoundException( Throwable cause )
    {
        super( cause );
    }

}
