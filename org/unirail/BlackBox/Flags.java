package org.unirail.BlackBox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Make enum as a bitflags set instead of simple constants set
@Target(ElementType.TYPE)
public @interface Flags {}
