package com.honeysaltlove.domain.app.service;

import com.honeysaltlove.domain.app.dto.AppKeyIssueResponse;
import com.honeysaltlove.domain.app.entity.MemberAppKey;
import com.honeysaltlove.domain.app.repository.MemberAppKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 앱 키 — 꿀방울 앱이 처음 실행할 때 받는다 (웹 계정 없이 앱만 써도 활동 기록이 쌓인다)
 * <pre>
 * - 앱 키: 어느 앱인지 알아보는 값 (요청 헤더 X-App-Key)
 * - 비밀값: 발급할 때 한 번만 준다 · 서버에는 SHA-256 해시만 저장한다
 * - 키 · 비밀값은 로그에 남기지 않는다 (가이드 보안 규칙)
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppKeyService {

    private static final int KEY_BYTES = 24;      // Base64URL 32자
    private static final int SECRET_BYTES = 32;   // Base64URL 43자
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final MemberAppKeyRepository appKeyRepository;

    /**
     * 앱 키를 새로 만든다
     * @author shinm
     * @since 2026-11-10
     */
    @Transactional
    public AppKeyIssueResponse issue() {
        String appKey = newAppKey();
        String appSecret = random(SECRET_BYTES);
        MemberAppKey saved = appKeyRepository.save(MemberAppKey.issue(appKey, hash(appSecret)));
        log.info("app key issued: appKeyId={}", saved.getId());
        return new AppKeyIssueResponse(appKey, appSecret);
    }

    /**
     * 비밀값의 해시 — 저장할 때와 앱이 보낸 비밀값을 확인할 때(12. 앱 연결) 같이 쓴다
     * @author shinm
     * @since 2026-11-10
     */
    public static String hash(String appSecret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(appSecret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);   // 64자 — app_hash varchar(64)
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }

    private String newAppKey() {
        String appKey;
        do {
            appKey = random(KEY_BYTES);   // 겹칠 일은 사실상 없지만 UQ 에 걸리지 않게 한 번 더 본다
        } while (appKeyRepository.findByAppKey(appKey).isPresent());
        return appKey;
    }

    private static String random(int bytes) {
        byte[] buf = new byte[bytes];
        RANDOM.nextBytes(buf);
        return ENCODER.encodeToString(buf);
    }
}
