/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.user;

import java.util.HashSet;
import java.util.Set;

import org.projectforge.database.XmlDumpHook;
import org.projectforge.database.xstream.XStreamSavingConverter;
import org.projectforge.registry.Registry;
import org.projectforge.task.TaskDO;
import org.projectforge.web.task.TaskTreePage;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class UserXmlPreferencesXmlDumpHook implements XmlDumpHook
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserXmlPreferencesXmlDumpHook.class);

  /**
   * @see org.projectforge.database.XmlDumpHook#onBeforeRestore(org.projectforge.database.xstream.XStreamSavingConverter, java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void onBeforeRestore(final XStreamSavingConverter xstreamSavingConverter, final Object obj)
  {
    if (obj instanceof UserXmlPreferencesDO) {
      final UserXmlPreferencesDO userPrefs = (UserXmlPreferencesDO) obj;
      if (TaskTreePage.USER_PREFS_KEY_OPEN_TASKS.equals(userPrefs.getKey()) == false) {
        return;
      }
      final UserXmlPreferencesDao userXmlPreferencesDao = Registry.instance().getUserXmlPreferencesDao();
      final Object userPrefsObj = userXmlPreferencesDao.deserialize(userPrefs, true);
      if (userPrefsObj == null || userPrefsObj instanceof Set == false) {
        return;
      }
      Set<Integer> oldIds = null;
      try {
        oldIds = (Set<Integer>) userPrefsObj;
      } catch (final ClassCastException ex) {
        log.error("Oups, Set of task id's is not of type Set<Integer>, can't migrate this list.");
      }
      if (oldIds.size() == 0) {
        return;
      }
      final Set<Integer> newIds = new HashSet<Integer>();
      for (final Integer oldId : oldIds) {
        final Integer newId = xstreamSavingConverter.getNewIdAsInteger(TaskDO.class, oldId);
        newIds.add(newId);
      }
      userXmlPreferencesDao.serialize(userPrefs, newIds);
      return;
    }
  }
}
