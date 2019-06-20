package com.earth2me.essentials.storage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>MapValueType class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
@Target(ElementType.FIELD) @Retention(RetentionPolicy.RUNTIME) public @interface MapValueType {
    Class value() default String.class;
}
