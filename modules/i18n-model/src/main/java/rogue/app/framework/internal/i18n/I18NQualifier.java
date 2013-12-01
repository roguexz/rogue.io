package rogue.app.framework.internal.i18n;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * General purpose qualifier annotation that is used within the i18n-model & i18n-view modules.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface I18NQualifier
{
    /**
     * A generic value for scenarios where the same qualifier needs to be differentiated.
     */
    Class<?> value() default I18NQualifier.class;
}
