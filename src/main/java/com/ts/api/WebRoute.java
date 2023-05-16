package com.ts.api;

import com.ts.vo.TsJobResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/ts-job/template")
public class WebRoute {
    private final Logger log = LoggerFactory.getLogger(WebRoute.class);

    @RequestMapping("/login")
    public TsJobResponseVO<String> login(String username, String password){
        log.info("user {} start login ts-job platform at {}", username, new Date());
        if (username.equals("admin") && password.equals("woainizhongguo")) {
            return new TsJobResponseVO<>(200L,"登陆成功");
        } else {
            return new TsJobResponseVO<>(400L,"用户名密码错误");
        }
    }
}
