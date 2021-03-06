/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.database.xstream;

import org.hibernate.proxy.HibernateProxy;

import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

class HibernateProxyConverter extends ReflectionConverter
{
  private ConverterLookup converterLookup;

  public HibernateProxyConverter(Mapper arg0, ReflectionProvider arg1, ConverterLookup converterLookup)
  {
    super(arg0, arg1);
    this.converterLookup = converterLookup;
  }

  /**
   * be responsible for hibernate proxy
   */
  public boolean canConvert(Class clazz)
  {
    // System.err.println("converter says can convert " + clazz + ":"+ HibernateProxy.class.isAssignableFrom(clazz));
    return HibernateProxy.class.isAssignableFrom(clazz);
  }

  public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    // System.err.println("converter marshalls: " + ((HibernateProxy)arg0).getHibernateLazyInitializer().getImplementation());
    Object item = ((HibernateProxy) arg0).getHibernateLazyInitializer().getImplementation();
    converterLookup.lookupConverterForType(item.getClass()).marshal(item, writer, context);
  }
}
