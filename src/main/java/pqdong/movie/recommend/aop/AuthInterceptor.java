package pqdong.movie.recommend.aop;


import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pqdong.movie.recommend.annotation.AuthCheck;
import pqdong.movie.recommend.common.ErrorCode;
import pqdong.movie.recommend.enums.UserRoleEnum;
import pqdong.movie.recommend.exception.BusinessException;
import pqdong.movie.recommend.exception.MyException;
import pqdong.movie.recommend.exception.ResultEnum;
import pqdong.movie.recommend.mongo.service.UserMongoService;

import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.RecommendUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserMongoService userMongoService;




    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        String token = RecommendUtils.getToken(request);
        if (StringUtils.isEmpty(token)){
            throw new MyException(ResultEnum.NEED_LOGIN);
        }
        UserTemp loginUser = userMongoService.getUserInfo(token);
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = loginUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }
        }


        return joinPoint.proceed();
    }

    public static void main(String[] args) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int day = LocalDateTime.now().getDayOfMonth();
        System.out.println(day);
    }
}

