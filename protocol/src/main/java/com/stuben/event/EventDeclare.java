package com.stuben.event;

import com.stuben.serializer.FastJsonSerializer;
import com.stuben.serializer.ISerializer;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventDeclare {
    /**
     * 是否可以分享
     */
    boolean share() default false;

    /**
     * 事件来源
     */
    String source() default "";

    /**
     * 事件名称
     */
    String name() default "";

    /**
     * 序列化类
     */
    Class<? extends ISerializer> serializer() default FastJsonSerializer.class;
}
