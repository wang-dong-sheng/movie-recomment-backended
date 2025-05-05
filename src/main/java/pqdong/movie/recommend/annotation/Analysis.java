package pqdong.movie.recommend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员统计分析系统
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Analysis {
    /**
     * 访问的接口操作：主要是两个一个是：查看电影详情信息
     *
     * @return
     */
    String opt() default "";
}

