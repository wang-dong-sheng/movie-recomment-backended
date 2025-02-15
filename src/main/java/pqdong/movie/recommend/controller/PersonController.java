package pqdong.movie.recommend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pqdong.movie.recommend.domain.util.ResponseMessage;
import pqdong.movie.recommend.service.PersonService;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/person")
public class PersonController {

    @Resource
    private PersonService personService;

    /**
     * @param key  关键字
     * @param page 当前页数
     * @param size 每页数据量
     * @method allPerson 查看所有演员
     **/
    @GetMapping("/list")
    public ResponseMessage allPerson(
            @RequestParam(required = false, defaultValue = "") String key,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "4") int size) {
        return ResponseMessage.successMessage(personService.getAllPerson(key, page, size));
    }

    /**
     * @param personId 演员id
     * @method getPerson 获取导演演员详情
     **/
    @GetMapping("/info")
    public ResponseMessage getPerson(
            @RequestParam(required = true, defaultValue = "0") Long personId) {
        return ResponseMessage.successMessage(personService.getPerson(personId));
    }
}
