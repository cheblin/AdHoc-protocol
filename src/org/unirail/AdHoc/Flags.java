package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Make enum as a bitflags set instead of simple integer constants set
@Target(ElementType.TYPE)
public @interface Flags {}
