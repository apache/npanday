#region Apache License, Version 2.0
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
#endregion
using System;
using System.Collections.Generic;
using System.Text;


using NPanday.ProjectImporter.Digest;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Model
{

    public class WebReferenceUrl
    {
        private string urlBehavior;
        public string UrlBehavior
        {
            get { return urlBehavior; }
            set { urlBehavior = value; }
        }

        private string relPath;
        public string RelPath
        {
            get { return relPath; }
            set { relPath = value; }
        }

        private string updateFromURL;
        public string UpdateFromURL
        {
            get { return updateFromURL; }
            set { updateFromURL = value; }
        }
        
        private string serviceLocationURL;
        public string ServiceLocationURL
        {
            get { return serviceLocationURL; }
            set { serviceLocationURL = value; }
        }
        
        private string cachedDynamicPropName;
        public string CachedDynamicPropName
        {
            get { return cachedDynamicPropName; }
            set { cachedDynamicPropName = value; }
        }
        
        private string cachedAppSettingsObjectName;
        public string CachedAppSettingsObjectName
        {
            get { return cachedAppSettingsObjectName; }
            set { cachedAppSettingsObjectName = value; }
        }
        
        private string cachedSettingsPropName;
        public string CachedSettingsPropName
        {
            get { return cachedSettingsPropName; }
            set { cachedSettingsPropName = value; }
        }
    }

}
