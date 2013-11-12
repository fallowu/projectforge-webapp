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

package org.projectforge.multitenancy;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.projectforge.common.AbstractCache;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Caches the tenants.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TenantsCache extends AbstractCache
{
  private static Logger log = Logger.getLogger(TenantsCache.class);

  private HibernateTemplate hibernateTemplate;

  /** The key is the order id. */
  private Set<TenantDO> tenants;

  public boolean isEmpty()
  {
    checkRefresh();
    return CollectionUtils.isEmpty(tenants);
  }

  public TenantDO getTenant(final Integer id)
  {
    if (id == null) {
      return null;
    }
    checkRefresh();
    if (tenants == null) {
      return null;
    }
    for (final TenantDO tenant : tenants) {
      if (id.equals(tenant.getId()) == true) {
        return tenant;
      }
    }
    return null;
  }

  /**
   * @return the tenants
   */
  public Set<TenantDO> getTenants()
  {
    return tenants;
  }

  /**
   * This method will be called by CacheHelper and is synchronized via getData();
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void refresh()
  {
    log.info("Initializing TenantsCache ...");
    // This method must not be synchronized because it works with a new copy of maps.
    final Set<TenantDO> set = new TreeSet<TenantDO>(new TenantsComparator());
    final List<TenantDO> list = hibernateTemplate.find("from TenantDO t where deleted=false");
    for (final TenantDO tenant : list) {
      set.add(tenant);
    }
    this.tenants = set;
    log.info("Initializing of TenantsCache done.");
  }

  public void setHibernateTemplate(final HibernateTemplate hibernateTemplate)
  {
    this.hibernateTemplate = hibernateTemplate;
  }
}