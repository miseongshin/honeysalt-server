package com.honeysaltlove.global.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 설정 파일의 ENC(...) 값을 풀어 쓰는 jasypt 암호기. 마스터 키는 환경변수(JASYPT_ENCRYPTOR_PASSWORD)로 받는다
 *
 * @author shinm
 * @since 2026-01-08
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-01-08        shinm              최초 생성
 */
@Configuration
@EnableEncryptableProperties
public class JasyptConfig {

    private static final String ALGORITHM = "PBEWithMD5AndDES";
    private static final String KEY_OBFUSCATION_ITERATIONS = "1000";
    private static final String POOL_SIZE = "1";
    private static final String PROVIDER_NAME = "SunJCE";
    private static final String SALT_GENERATOR_CLASS = "org.jasypt.salt.RandomSaltGenerator";
    private static final String IV_GENERATOR_CLASS = "org.jasypt.iv.NoIvGenerator";
    private static final String STRING_OUTPUT_TYPE = "base64";

    @Value("${jasypt.encryptor.password}")
    private String encryptorPassword;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        config.setPassword(encryptorPassword); // 복호화에 사용할 마스터 키
        config.setAlgorithm(ALGORITHM); // 암호화 알고리즘
        config.setKeyObtentionIterations(KEY_OBFUSCATION_ITERATIONS);
        config.setPoolSize(POOL_SIZE);
        config.setProviderName(PROVIDER_NAME);
        config.setSaltGeneratorClassName(SALT_GENERATOR_CLASS);
        config.setIvGeneratorClassName(IV_GENERATOR_CLASS);
        config.setStringOutputType(STRING_OUTPUT_TYPE);

        encryptor.setConfig(config);
        return encryptor;
    }
}