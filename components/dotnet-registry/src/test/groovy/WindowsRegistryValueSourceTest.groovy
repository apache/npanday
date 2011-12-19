/* Copyright 2010 NPanday
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package npanday.registry.impl

import npanday.registry.WindowsRegistryAccessException
import npanday.registry.WindowsRegistryAccessProvider
import npanday.registry.WindowsRegistryAccessProvider.RegistryHKey
import org.gmock.GMockTestCase
import org.junit.Test

class WindowsRegistryValueSourceTest extends GMockTestCase
{
  @Test
  void testExceptionIsCaught()
  {
    def provider = mock(WindowsRegistryAccessProvider);

    provider.getValue(RegistryHKey.HKLM, "Key", "ValueName").raises(new WindowsRegistryAccessException("Trouble here!!"))

    play{
      def source = new WindowsRegistryValueSource(provider)

      assert source.getValue("HKLM\\Key@ValueName") == ""
    }
  }

  @Test
  void testWrongKeysAreNull()
  {
    def provider = mock(WindowsRegistryAccessProvider);

    provider.getValue(RegistryHKey.HKLM, "Key", "ValueName")
      .raises(new WindowsRegistryAccessException("Trouble here!!"))
      .stub()

    play {
      def source = new WindowsRegistryValueSource(provider)

      assert source.getValue("ABC\\Key@ValueName") == null
      assert source.getValue("HKLM\\Key?ValueName") == null
      assert source.getValue("HKLM") == null
    }
  }

  @Test
  void testExpressionParsing()
  {
    def provider = mock(WindowsRegistryAccessProvider);

    provider.getValue(RegistryHKey.HKLM, "Key", "ValueName").returns("value")

    play{
      def source = new WindowsRegistryValueSource(provider)

      assert source.getValue("HKLM\\Key@ValueName") == "value"
    }
  }

  @Test
  void testHKLMLongAndShortNames()
  {
    def provider = mock(WindowsRegistryAccessProvider);

    provider.getValue(RegistryHKey.HKLM, "Key", "ValueName")
      .returns("value")
      .times(2)

    play{
      def source = new WindowsRegistryValueSource(provider)

      assert source.getValue("HKLM\\Key@ValueName") == "value"
      assert source.getValue("HKEY_LOCAL_MACHINE\\Key@ValueName") == "value"
    }
  }

  @Test
  void testHKCULongAndShortNames()
  {
    def provider = mock(WindowsRegistryAccessProvider);

    provider.getValue(RegistryHKey.HKCU, "Key", "ValueName")
      .returns("value")
      .times(2)

    play{
      def source = new WindowsRegistryValueSource(provider)

      assert source.getValue("HKCU\\Key@ValueName") == "value"
      assert source.getValue("HKEY_CURRENT_USER\\Key@ValueName") == "value"
    }
  }
}
