package com.honeysaltlove.domain.member.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 닉네임을 비워 두면 만드는 이름 — 꾸밈말 + 장소 · 때 + 꿀 낱말 (예: 포근한숲속꿀벌) · 숫자를 붙이지 않는다
 * <pre>
 * - 조합은 30 × 30 × 30 = 27,000가지 · 겹치면 다시 뽑고, 50번 모두 겹치는 드문 경우에만 뒤에 1, 2, 3 … 을 붙인다 (MemberService — 빈 이름이 나올 때까지 · 가입이 막히지 않게)
 * - 그 경우 경고 로그(auto nickname collided)가 남는다 — 이 로그가 보이면 낱말을 늘린다
 * - 가장 길어도 3 + 2 + 4 = 9자라 2~12자 규칙 안에 든다 · 숫자를 붙여 12자를 넘으면 MemberService 가 새 이름을 뽑는다
 * - 마음 · 건강 상태로 읽히는 말(우울한 · 지친 · 아픈)과 성별 · 나이를 떠올리게 하는 말은 쓰지 않는다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 * 2026-11-11        shinm              숫자를 붙이는 일은 MemberService 로 (1, 2, 3 … 차례로)
 */
@Component
public class NicknameGenerator {

    // 한국어 낱말 (_KO) — 다른 언어 이름은 3차 다국어 때 _EN 처럼 더한다
    private static final List<String> FIRSTS_KO = List.of(
            "포근한", "말랑한", "느긋한", "다정한", "산뜻한", "보송한", "달콤한", "향긋한", "싱그런", "따뜻한",
            "고소한", "촉촉한", "맑은", "보드란", "씩씩한", "명랑한", "상냥한", "소담한", "아늑한", "총총",
            "반짝", "살랑", "몽글", "동글", "폭신", "사뿐", "방긋", "꼬물", "졸졸", "새콤");

    private static final List<String> MIDDLES_KO = List.of(
            "아침", "새벽", "노을", "한낮", "별밤", "달빛", "봄날", "여름", "가을", "겨울",
            "오후", "저녁", "햇볕", "눈꽃", "새봄", "뒤뜰", "골목", "바다", "마당", "산책",
            "숲속", "들판", "언덕", "냇가", "정원", "꽃밭", "구름", "바람", "하늘", "호수");

    private static final List<String> LASTS_KO = List.of(
            "꿀벌", "꿀방울", "벌집", "꽃잎", "민들레", "클로버", "솜사탕", "햇살", "이슬", "쿠키",
            "꿀떡", "찐빵", "앵두", "딸기", "자두", "보리", "해바라기", "부엉이", "펭귄", "고슴도치",
            "나비", "꽃송이", "새싹", "도토리", "다람쥐", "토끼", "고양이", "강아지", "참새", "꿀단지");

    /**
     * 이름 하나를 만든다 (겹치는지는 부르는 쪽이 확인한다)
     * @author shinm
     * @since 2026-11-10
     */
    public String generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return pick(FIRSTS_KO, random) + pick(MIDDLES_KO, random) + pick(LASTS_KO, random);
    }

    private static String pick(List<String> words, ThreadLocalRandom random) {
        return words.get(random.nextInt(words.size()));
    }
}
