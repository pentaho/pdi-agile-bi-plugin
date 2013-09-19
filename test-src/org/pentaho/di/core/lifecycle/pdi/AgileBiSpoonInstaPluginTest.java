/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.di.core.lifecycle.pdi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pentaho.agilebi.spoon.perspective.AgileBiSpoonInstaPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.test.platform.engine.core.PluginManagerAdapter;

public class AgileBiSpoonInstaPluginTest {

  @Test
  public void isInstaviewRegistered_true() {
    IPluginManager mgr = new PluginManagerAdapter() {
      public List<String> getRegisteredPlugins() {
        return Arrays.asList("instaview");
      }
    };
    assertTrue(AgileBiSpoonInstaPlugin.isInstaviewRegistered(mgr));
  }

  @Test
  public void isInstaviewRegistered_false() {
    IPluginManager mgr = new PluginManagerAdapter() {
      public List<String> getRegisteredPlugins() {
        return Arrays.asList("not instaview");
      }
    };
    assertFalse(AgileBiSpoonInstaPlugin.isInstaviewRegistered(mgr));
  }

}
