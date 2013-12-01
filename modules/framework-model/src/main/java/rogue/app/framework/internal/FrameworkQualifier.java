package rogue.app.framework.internal;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * General purpose qualifier annotation that is used within the framework-model & framework-view modules.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface FrameworkQualifier
{
    /**
     * A generic value for scenarios where the same qualifier needs to be differentiated.
     */
    Class<?> value() default FrameworkQualifier.class;
}
