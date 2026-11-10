package com.honeysaltlove.domain.member.entity;

import com.honeysaltlove.global.common.BaseTimeEntity;
import com.honeysaltlove.global.common.Lang;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 약관 원문과 버전. 시행 전 초안은 고칠 수 있고, 시행 뒤에는 고치지 않고 새 버전이 등록된다
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 * 2026-11-10        shinm              서비스(service) · 언어(lang) 추가 — 꿀방울 약관도 담는다
 */
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_terms_service_type_lang_version",
        columnNames = {"service", "type", "lang", "version"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceType service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TermsType type;

    @Column(nullable = false, length = 20)
    private Lang lang;                         // 약관 언어 · DB 에는 ko · en … (LangConverter)

    @Column(nullable = false, length = 20)
    private String version;                    // 예: 2026-11

    @Column(nullable = false, columnDefinition = "text")
    private String body;                       // @Lob 대신 text — PostgreSQL 은 text 에 길이 제한이 없다

    @Column(nullable = false)
    private boolean required;                  // true 면 동의해야 가입 · 사용

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;               // 시행일

    private Terms(ServiceType service, TermsType type, Lang lang, String version, String body,
                  boolean required, Instant effectiveAt) {
        this.service = service;
        this.type = type;
        this.lang = lang;
        this.version = version;
        this.body = body;
        this.required = required;
        this.effectiveAt = effectiveAt;
    }

    /**
     * 새 약관을 등록한다
     * @author shinm
     * @since 2026-11-04
     */
    public static Terms create(ServiceType service, TermsType type, Lang lang, String version, String body,
                               boolean required, Instant effectiveAt) {
        return new Terms(service, type, lang, version, body, required, effectiveAt);
    }

    /**
     * 초안 고치기. 시행일이 지났으면 고치지 않는다 — 동의한 사람이 본 글이 바뀌면 안 된다
     * @author shinm
     * @since 2026-11-04
     */
    public void editDraft(String body, Instant effectiveAt, Instant now) {
        if (!now.isBefore(this.effectiveAt)) {
            throw new IllegalStateException("시행된 약관은 고치지 않는다. 새 버전을 넣는다");
        }
        this.body = body;
        this.effectiveAt = effectiveAt;
    }
}
