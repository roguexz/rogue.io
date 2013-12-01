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

package rogue.app.framework.support.datanucleus;

import com.google.common.base.Preconditions;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jpa.JPAEntityManagerFactory;
import org.datanucleus.api.jpa.PersistenceProviderImpl;
import org.datanucleus.api.jpa.SingletonEMFException;
import org.datanucleus.api.jpa.exceptions.NoPersistenceUnitException;
import org.datanucleus.api.jpa.exceptions.NoPersistenceXmlException;
import org.datanucleus.api.jpa.exceptions.NotProviderException;
import org.datanucleus.metadata.MetaDataUtils;
import org.datanucleus.metadata.PersistenceFileMetaData;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.datanucleus.plugin.PluginManager;
import rogue.app.framework.internal.util.ClassUtils;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An extension of the DataNucleus persistence provider that honors the modular packaging of persistence classes.
 * <p/>
 * The assumption here is that there won't be more than one persistence unit defined for the entire application. If you
 * have more than one unit, then you will need to tweak this class to suit your needs.
 */
public class GAEPersistenceProviderImpl extends PersistenceProviderImpl
{
    private static final String CLASS_NAME = GAEPersistenceProviderImpl.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public GAEPersistenceProviderImpl()
    {
        super();
        LOGGER.logp(Level.INFO, CLASS_NAME, "<ctor>", "Created a new instance of " + getClass().getSimpleName());
    }

    /**
     * Overrides the parent method so that we can add the enhanced classes manually to the PU metadata.
     */
    @Override
    public EntityManagerFactory createEntityManagerFactory(String unitName, Map overridingProps)
    {
        final String METHOD_NAME = "createEntityManagerFactory";

        try
        {
            PersistenceUnitMetaData unitMetaData = getPersistenceUnitMetaData(unitName, overridingProps);
            if (unitMetaData != null)
            {
                // Reset the provider implementation in the PU metadata before passing it forward.
                unitMetaData.setProvider(PersistenceProviderImpl.class.getName());
                associatePersistenceClasses(unitMetaData);
                return new JPAEntityManagerFactory(unitMetaData, overridingProps);
            }
            else
            {
                // No "persistence-unit" of the same name as requested so nothing to manage the persistence of
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "No persistence unit found.");
            }
        }
        catch (SingletonEMFException se)
        {
            return se.getSingleton();
        }
        catch (NotProviderException | NoPersistenceUnitException | NoPersistenceXmlException e)
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Unable to get persistence unit.", e);
        }

        return null;
    }

    /**
     * Copied over from JPAEntityManagerFactory from DataNucleus code. Modify it as and when the base code changes.
     */
    private PersistenceUnitMetaData getPersistenceUnitMetaData(String unitName, Map overridingProps)
    {
        final String METHOD_NAME = "getPersistenceUnitMetaData";
        PersistenceUnitMetaData unitMetaData = null;

        // Find all "META-INF/persistence.xml" files in the current thread loader CLASSPATH and parse them
        PluginManager pluginMgr =
                PluginManager.createPluginManager(overridingProps, this.getClass().getClassLoader());
        String persistenceFileName = null;
        boolean validate = false;
        if (overridingProps != null)
        {
            if (overridingProps.containsKey(PropertyNames.PROPERTY_PERSISTENCE_XML_FILENAME))
            {
                persistenceFileName = (String) overridingProps.get(PropertyNames.PROPERTY_PERSISTENCE_XML_FILENAME);
            }
            if (overridingProps.containsKey(PropertyNames.PROPERTY_METADATA_VALIDATE))
            {
                validate =
                        Boolean.getBoolean((String) overridingProps.get(PropertyNames.PROPERTY_METADATA_VALIDATE));
            }
        }
        PersistenceFileMetaData[] files = MetaDataUtils.parsePersistenceFiles(pluginMgr, persistenceFileName,
                                                                              validate, null);
        if (files == null)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "No persistence.xml files found.");
        }
        else
        {
            for (PersistenceFileMetaData file : files)
            {
                PersistenceUnitMetaData[] unitmds = file.getPersistenceUnits();
                for (PersistenceUnitMetaData unitmd : unitmds)
                {
                    if (unitmd.getName().equals(unitName))
                    {
                        unitMetaData = unitmd;
                        // unitMetaData.clearJarFiles(); // Jar files not applicable to J2SE [JPA 6.3]
                        break;
                    }
                }
            }
        }
        return unitMetaData;
    }

    /**
     * Looks for all jars containing META-INF/orm.xml and then searches for classes that contain the required
     * annotations.
     */
    private void associatePersistenceClasses(PersistenceUnitMetaData metaData)
    {
        final String METHOD_NAME = "associatePersistenceClasses";
        Preconditions.checkNotNull(metaData, "PU Metadata cannot be null");
        Iterator<Class<?>> iterator = ClassUtils.getTypesAnnotatedWith(Entity.class);
        while (iterator.hasNext())
        {
            metaData.addClassName(iterator.next().getName());
        }
    }
}
