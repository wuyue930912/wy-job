package com.ts.api;

import com.ts.TsJobProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录 API
 * 
 * @author yue.wu
 */
@Slf4j
@RestController
@RequestMapping("/ts-job/api")
@RequiredArgsConstructor
public class LoginApi {

    private final TsJobProperties properties;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, 
                                      @RequestParam String password,
                                      HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        // 如果未启用登录，直接成功
        if (!Boolean.TRUE.equals(properties.getLogin().getEnabled())) {
            result.put("code", 200);
            result.put("message", "登录成功");
            return result;
        }
        
        // 验证用户名密码
        String configUsername = properties.getLogin().getUsername();
        String configPassword = properties.getLogin().getPassword();
        
        if (configUsername.equals(username) && configPassword.equals(password)) {
            session.setAttribute("isLoggedIn", true);
            session.setAttribute("username", username);
            log.info("[ts-job] 用户 {} 登录成功", username);
            result.put("code", 200);
            result.put("message", "登录成功");
        } else {
            log.warn("[ts-job] 用户 {} 登录失败，密码错误", username);
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
        }
        
        return result;
    }
    
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "退出成功");
        return result;
    }
}
