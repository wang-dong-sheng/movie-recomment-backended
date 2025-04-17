package pqdong.movie.recommend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pqdong.movie.recommend.annotation.AuthCheck;
import pqdong.movie.recommend.annotation.LoginRequired;
import pqdong.movie.recommend.common.PageRequest;
import pqdong.movie.recommend.data.constant.UserConstant;
import pqdong.movie.recommend.data.dto.user.UserQueryRequest;
import pqdong.movie.recommend.data.entity.User;
import pqdong.movie.recommend.data.entity.UserEntity;
import pqdong.movie.recommend.data.dto.user.UserInfo;
import pqdong.movie.recommend.domain.util.ResponseMessage;
import pqdong.movie.recommend.mongo.model.request.LoginUserRequest;
import pqdong.movie.recommend.mongo.model.request.RegisterUserRequest;
import pqdong.movie.recommend.mongo.service.UserMongoService;
import pqdong.movie.recommend.newService.UserNewService;
import pqdong.movie.recommend.redis.RedisApi;
import pqdong.movie.recommend.service.jpa.SmsService;
import pqdong.movie.recommend.service.jpa.UserService;
import pqdong.movie.recommend.service.mabatis.UserMybatisService;
import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.Md5EncryptionHelper;
import pqdong.movie.recommend.utils.RecommendUtils;


import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * UserController
 * @description 用户信息相关接口
 * @author pqdong
 * @since 2020/02/27 16:42
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private RedisApi redisApi;
    @Resource
    private SmsService smsService;

//    @Resource
//    private UserService userService;
//    @Resource
//    private UserMybatisService userMybatisService;
//    @Resource
//    private UserMongoService userMongoService;

    @Resource
    private UserNewService userNewService;

    /**
     * @method getUserInfo 获取用户信息
     */
    @GetMapping("/userInfo")
    public ResponseMessage getCourseInfo(@RequestParam(required = true) String token) {
        return ResponseMessage.successMessage(userNewService.getUserInfo(token));
    }

    /**
     * @method updateUserInfo 修改用户信息
     */
    @PostMapping("/userInfo")
    public ResponseMessage updateUserInfo(@RequestBody(required = true) UserTemp user) {
        userNewService.updateUser(user);
        if (null == user){
            return ResponseMessage.failedMessage("昵称已经存在，请跟换昵称！");
        }
        return ResponseMessage.successMessage(userNewService.updateUser(user));
    }

    /**
     * @method register 注册用户
     */
    @PostMapping("/register")
    public ResponseMessage register(@RequestBody UserInfo user){
        if(userNewService.checkUserExist(user.getUsername())){
            return ResponseMessage.failedMessage("该用户名已被注册");
        }

        boolean b = userNewService.registerUser(user);
        if (b){
            return ResponseMessage.successMessage("success");
        } else{
            return ResponseMessage.failedMessage("注册失败");
        }
    }

    /**
     * @method login 登录接口
     */
    @PostMapping("/login")
    public ResponseMessage userLogin(@RequestBody UserInfo user) {
        Map<String, Object> info = userNewService.login(user);
        if (info != null){
            return ResponseMessage.successMessage(info);
        } else {
            return ResponseMessage.failedMessage("登录失败，请检查用户名或密码！");
        }

    }

    /**
     * @method code 发送短信验证码的接口
     * @param phone 手机号
     **/
    @GetMapping("/code")
    public ResponseMessage code(@RequestParam String phone) {
        String code = smsService.sendCode(phone);
        if (StringUtils.isNotEmpty(code)) {
            return ResponseMessage.successMessage("发送成功");
        } else {
            return ResponseMessage.failedMessage("发送失败");
        }
    }

    /**
     * @method upload 上传用户头像
     * @param avatar 头像
     **/
    @PostMapping("/avatar")
    @LoginRequired
    public ResponseMessage upload(@RequestParam("userMd") String userMd, @RequestParam("avatar") MultipartFile avatar) {
        String url = userNewService.uploadAvatar(userMd, avatar);
        if (StringUtils.contains(url,"http")) {
            return ResponseMessage.successMessage(url);
        } else {
            return ResponseMessage.failedMessage(url);
        }
    }

    /**
     * @method logout 退出接口
     **/
    @PostMapping("/logout")
    @LoginRequired
    public ResponseMessage logout() {
        return ResponseMessage.successMessage(userNewService.logout());
    }

    /*
    * 获取所有用户信息
    * */
    @LoginRequired
    @GetMapping("/getAllUser")
    public ResponseMessage getAllUser( PageRequest pageRequest){


        return ResponseMessage.successMessage(userNewService.getAllUser(pageRequest));
    }


    /*
    删除用户信息
    * */
    @DeleteMapping("/deleteUsers")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> deleteUsers(@RequestBody List<Long> ids){


        return ResponseMessage.successMessage(userNewService.deleteUsers(ids));
    }

    /**
     * 按条件查询用户相关信息
     *
     */
    @PostMapping("/filterUsers")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Page<UserTemp>> filterUsers(@RequestBody UserQueryRequest userQueryRequest){

        log.info(JSONUtil.toJsonStr(userQueryRequest));

        return ResponseMessage.successMessage(userNewService.filterUsers(userQueryRequest));
    }
}
