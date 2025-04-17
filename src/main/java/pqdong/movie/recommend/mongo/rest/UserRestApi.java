package pqdong.movie.recommend.mongo.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pqdong.movie.recommend.mongo.model.domain.User;
import pqdong.movie.recommend.mongo.model.request.LoginUserRequest;
import pqdong.movie.recommend.mongo.model.request.RegisterUserRequest;
import pqdong.movie.recommend.mongo.service.UserMongoService;
import pqdong.movie.recommend.temp.UserTemp;

import java.util.Arrays;


@RequestMapping("/rest/users")
@Controller
public class UserRestApi {

    @Autowired
    private UserMongoService userService;

    @RequestMapping(value = "/login", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) {
//        User user  =userService.loginUser(new LoginUserRequest(username,password));

        return "ok";
    }

//    @RequestMapping(value = "/register", produces = "application/json", method = RequestMethod.GET)
//    @ResponseBody
//    public Model addUser(@RequestParam("username") String username,@RequestParam("password") String password,Model model) {
//        if(userService.checkUserExist(username)){
//            model.addAttribute("success",false);
//            model.addAttribute("message"," 用户名已经被注册！");
//            return model;
//        }
//        model.addAttribute("success",userService.registerUser(new RegisterUserRequest(username,password)));
//        return model;
//    }

    //冷启动问题
    @RequestMapping(value = "/pref", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model addPrefGenres(@RequestParam("username") String username,@RequestParam("genres") String genres,Model model) {
        UserTemp user = userService.findByUsername(username);
        user.getPrefGenres().addAll(Arrays.asList(genres.split(",")));
        user.setFirst(false);
        model.addAttribute("success",userService.updateUser(user));
        return model;
    }
}
