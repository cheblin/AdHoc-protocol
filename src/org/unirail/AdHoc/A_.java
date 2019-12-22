package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Optional field that value has up direction (higher) values dispersion gradient.
@Target(ElementType.FIELD)
public @interface A_ {
	double value() default 0;//most often lowest possible value or value range (-7 | 78)
}
	