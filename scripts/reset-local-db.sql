-- ======================================================================
-- 로컬 개발 DB(love_local)를 통째로 비운다 — 다음에 앱을 띄우면 Flyway 가 V1 부터 다시 만든다
-- 테이블을 하나씩 지우지 않는다: 시퀀스 · 타입 · flyway_schema_history 가 남아 Flyway 가 멈춘다
--   ("Found non-empty schema(s) "public" but no schema history table")
--
-- 실행: IntelliJ DB 콘솔에서 이 파일 전체를 실행한다
-- 안전장치: 지우는 명령이 DB 이름 검사 안에 있다 — love_local 이 아니면(운영 love 등) 오류로 끝나고 아무것도 지우지 않는다
--
-- @author shinm
-- @since 2026-11-10
-- ======================================================================
-- DATE              AUTHOR             NOTE
-- ----------------------------------------------------------------------
-- 2026-11-10        shinm              최초 생성
-- 2026-11-10        shinm              DB 이름 검사 안에서만 지운다 (결과를 보기 전에 지워지던 것을 막는다)
-- ======================================================================

do $$
begin
    if current_database() <> 'love_local' then
        raise exception 'love_local 이 아니다 (지금: %) — 아무것도 지우지 않았다', current_database();
    end if;
    execute 'drop schema public cascade';
    execute 'create schema public';
end
$$;
