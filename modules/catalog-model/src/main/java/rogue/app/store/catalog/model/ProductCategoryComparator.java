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

package rogue.app.store.catalog.model;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class ProductCategoryComparator implements Comparator<ProductCategory>, Serializable
{
    @Override
    public int compare(ProductCategory cat1, ProductCategory cat2)
    {
        if (Objects.equals(cat1, cat2))
        {
            return 0;
        }
        if (cat1 == null)
        {
            return -1;
        }
        if (cat2 == null)
        {
            return 1;
        }
        int compareValue = 0;
        compareValue = compare(cat1.getParent(), cat2.getParent());
        if (compareValue == 0)
        {
            compareValue = cat1.getName().compareTo(cat2.getName());
        }
        return compareValue;
    }
}
