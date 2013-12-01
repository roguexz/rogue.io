/*
 * Copyright 2013, Rogue.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rogue.app.framework.view.faces.model;

import org.primefaces.model.SelectableDataModel;
import rogue.app.framework.model.Identity;

import javax.faces.model.ListDataModel;
import java.io.Serializable;
import java.util.List;

/**
 * A generic list based data model that implements the <code>SelectableDataModel</code> interface. This data model
 * accepts a list of {@link rogue.app.framework.model.Identity identity} objects.
 */
public final class GenericListDataModel<E extends Identity> extends ListDataModel<E>
        implements SelectableDataModel, Serializable
{
    public GenericListDataModel()
    {
        super();
    }

    public GenericListDataModel(List<E> list)
    {
        super(list);
    }

    @Override
    public Object getRowKey(Object object)
    {
        return object != null && (object instanceof Identity) ? ((Identity) object).getId() : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getRowData(String rowKey)
    {
        List<E> list = (List<E>) this.getWrappedData();
        if (list != null)
        {
            for (Identity id : list)
            {
                if (id.getId().equals(rowKey))
                {
                    return id;
                }
            }
        }
        return null;
    }
}
