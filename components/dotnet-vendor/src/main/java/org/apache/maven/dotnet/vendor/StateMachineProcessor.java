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
package npanday.vendor;

/**
 * Provides services for filling in missing vendor info according to its state of completion. An implementation of this
 * class can use various <code>VendorInfoTransitionRule</code> instances to transition states during processing.
 *
 * @author Shane Isbell
 * @see VendorInfoTransitionRule
 */
public interface StateMachineProcessor
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = StateMachineProcessor.class.getName();

    /**
     * Processes the specified vendor info by filling in missing information.
     *
     * @param vendorInfo the vendor info to fill in
     * @throws IllegalStateException if the state of the specified vendor info is illegal or cannot be determined
     */
    void process( VendorInfo vendorInfo )
        throws IllegalStateException;

}
