package com.stuben.evenx.consumer;

import java.lang.annotation.*;

import org.apache.commons.lang3.StringUtils;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EvenxConsumerController {
    String value() default StringUtils.EMPTY;
}
