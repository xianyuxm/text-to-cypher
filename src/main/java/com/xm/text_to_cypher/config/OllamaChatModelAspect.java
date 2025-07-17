package com.xm.text_to_cypher.config;

import cn.hutool.core.util.StrUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OllamaChatModelAspect {

    @Value("${spring.ai.ollama.chat.model}")
    private String model;

    private String callStringMessage = "String org.springframework.ai.chat.model.ChatModel.call(String)";


    /**
     * 修改ollama的call方法，添加/no_think参数，返回结果去掉think标签
     * @param joinPoint joinPoint
     * @return Object
     * @throws Throwable
     */
    @Around("execution(* org.springframework.ai.chat.model.ChatModel.call(..))")
    public Object aroundMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        String signature = joinPoint.getSignature().toString();
        // 获取原始参数
        Object[] args = joinPoint.getArgs();
        // 如果是String类型的call方法，修改其参数
        /*if (StrUtil.equals(signature, callStringMessage) && args.length > 0) {
            args[0] = args[0] + "\n /no_think";
        }*/
        // 执行原方法
        Object result = joinPoint.proceed(args);
        if (StrUtil.equals(model,"qwen3:30b-a3b")|| StrUtil.equals(model,"qwen3:32b")) {
            if(StrUtil.equals(signature, callStringMessage)){
                result = ((String) result).replaceAll("(?is)<think\\b[^>]*>(.*?)</think>", "").trim();
            }
        }
        return result;
    }
}
