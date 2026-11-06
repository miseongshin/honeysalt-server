package com.honeysaltlove.domain.tag.entity;

import com.honeysaltlove.global.common.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용이 차단된 태그.
 * <pre>
 * - 운영자가 광고 · 욕설 태그의 사용을 차단한다
 * - 차단할 때 tag 행과 연결 행(○○_tag)을 지우고, 이름을 여기에 옮긴다 — tag 와 연결하지 않는다
 * - 조회 로직은 바꾸지 않는다. 태그를 만들 때 같은 이름이 여기 있는지 확인하고, 있으면 등록을 거부한다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagBlocklist extends BaseCreatedEntity {

    @Id
    @Column(length = 50)
    private String name;                       // 차단한 태그 이름

    @Column(nullable = false, length = 200)
    private String reason;                     // 차단 사유 · AI 가 차단하면 "AI: 광고" 처럼

    /**
     * 태그 이름의 사용을 차단한다. tag 행 삭제는 호출한 서비스에서 한다
     * @author shinm
     * @since 2026-11-06
     */
    public static TagBlocklist block(String name, String reason) {
        TagBlocklist b = new TagBlocklist();
        b.name = Tag.normalize(name);   // 태그를 만들 때와 같은 규칙으로
        b.reason = reason;
        return b;
    }
}
