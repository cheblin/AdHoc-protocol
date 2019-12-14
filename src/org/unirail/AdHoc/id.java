package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Pack unique identification. Applied by the code generator.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface id {
	long value();
}
