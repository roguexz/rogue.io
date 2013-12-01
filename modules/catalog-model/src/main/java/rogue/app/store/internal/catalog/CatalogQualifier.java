package rogue.app.store.internal.catalog;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * General purpose qualifier annotation that is used within the catalog-model & catalog-view modules.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface CatalogQualifier
{
    /**
     * A generic value for scenarios where the same qualifier needs to be differentiated.
     */
    Class<?> value() default CatalogQualifier.class;
}
