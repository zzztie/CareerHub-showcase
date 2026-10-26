package com.zzztie.careerhub.service.impl;

import com.zzztie.careerhub.service.VerificationCodeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeServiceImpl
        implements VerificationCodeService {

    private static final String EMAIL_CODE_KEY_PREFIX =
            "verification:email:";

    private static final long EMAIL_CODE_TTL_MINUTES = 5;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final StringRedisTemplate stringRedisTemplate;

    public VerificationCodeServiceImpl(
            StringRedisTemplate stringRedisTemplate
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void sendEmailCode(String email) {

        String code = generateCode();

        String key = buildEmailCodeKey(email);

        stringRedisTemplate.opsForValue().set(
                key,
                code,
                EMAIL_CODE_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        // 以后这里再接真正的邮件发送服务
    }

    @Override
    public boolean verifyEmailCode(
            String email,
            String code
    ) {

        String key = buildEmailCodeKey(email);

        String savedCode =
                stringRedisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            return false;
        }

        if (!savedCode.equals(code)) {
            return false;
        }

        stringRedisTemplate.delete(key);

        return true;
    }

    private String buildEmailCodeKey(String email) {
        return EMAIL_CODE_KEY_PREFIX + email;
    }

    private String generateCode() {
        int number = SECURE_RANDOM.nextInt(1_000_000);

        return String.format("%06d", number);
    }
}
