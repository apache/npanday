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
package npanday.executable.compiler;

/**
 * Provides services for obtaining information about the key file.
 *
 * @author Shane Isbell
 */
public interface KeyInfo
{
    /**
     * Returns the path of the key
     *
     * @return the path of the key
     */
    String getKeyFileUri();

    /**
     * Sets the path of the key
     *
     * @param keyFileUri the path of the key
     */
    void setKeyFileUri(String keyFileUri);

    String getKeyContainerName();

    void setKeyContainerName(String keyContainerName);

    public static class Factory
    {
        /**
         * Constructor
         */
        private Factory()
        {
        }

        /**
         * Returns a default instance of key info.
         *
         * @return a default instance of key info
         */
        public static KeyInfo createDefaultKeyInfo()
        {
            return new KeyInfo()
            {
                private String keyFileUri;

                private String keyContainerName;

                public String getKeyFileUri() {
                    return keyFileUri;
                }

                public void setKeyFileUri(String keyFileUri) {
                    this.keyFileUri = keyFileUri;
                }

                public String getKeyContainerName() {
                    return keyContainerName;
                }

                public void setKeyContainerName(String keyContainerName) {
                    this.keyContainerName = keyContainerName;
                }

            };
        }
    }

}
