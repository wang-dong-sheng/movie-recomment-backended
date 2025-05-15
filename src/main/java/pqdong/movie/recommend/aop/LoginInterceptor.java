package pqdong.movie.recommend.aop;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import pqdong.movie.recommend.annotation.LoginRequired;
import pqdong.movie.recommend.constant.UserConstant;
import pqdong.movie.recommend.exception.MyException;
import pqdong.movie.recommend.exception.ResultEnum;
import pqdong.movie.recommend.service.UserNewService;
import pqdong.movie.recommend.redis.RedisApi;
import pqdong.movie.recommend.redis.RedisKeys;
import pqdong.movie.recommend.data.entity.UserTemp;
import pqdong.movie.recommend.utils.RecommendUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * LoginInterceptor
 *
 */
@Slf4j
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static LoginInterceptor loginInterceptor;
    @Resource
    private UserNewService userNewService;

    @Resource
    private RedisApi redis;


    @PostConstruct
    public void init() {
        loginInterceptor = this;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        Method method = ((HandlerMethod) handler).getMethod();
        // 判断需要调用需要登陆的接口时是否已经登陆
        boolean isLoginRequired = isAnnotationPresent(method, LoginRequired.class);
        if (isLoginRequired) {
            String uri = request.getRequestURI();
            String token = RecommendUtils.getToken(request);
            if (StringUtils.isEmpty(token)){
                throw new MyException(ResultEnum.NEED_LOGIN);
            }
            String userId = loginInterceptor.redis.getString(RecommendUtils.getKey(RedisKeys.USER_TOKEN, token));
            if (StringUtils.isEmpty(userId)){
                // 没有获取到redis中的信息
                throw new MyException(ResultEnum.NEED_LOGIN);
            }
            UserTemp user = loginInterceptor.userNewService.findByUID(Integer.valueOf(userId));
            if (user == null) {
                // token无法获取到用户信息代表未登陆
                throw new MyException(ResultEnum.NEED_LOGIN);
            }
            // 退出时删除缓存
            if (uri.contains(UserConstant.LOGOUT)) {
                loginInterceptor.redis.delKey(RecommendUtils.getKey(RedisKeys.USER_TOKEN, token));
            }
        }
        return true;
    }

    private boolean isAnnotationPresent(Method method, Class<? extends Annotation> annotationClass) {
        // 查找类注解或者方法注解
        return method.getDeclaringClass().isAnnotationPresent(annotationClass) || method.isAnnotationPresent(annotationClass);
    }

}