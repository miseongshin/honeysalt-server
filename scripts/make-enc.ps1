# ======================================================================
# DB 비밀번호 두 개(개발 · 운영)를 jasypt ENC(...) 로 바꾼다
# 마스터 키는 직접 치지 않는다 — 앱이 쓰는 키를 그대로 가져와서, 키가 어긋날 일이 없다
#   1) 환경변수 JASYPT_ENCRYPTOR_PASSWORD
#   2) 없으면 IntelliJ 실행 설정(.idea/workspace.xml)의 같은 이름 값
# 입력한 비밀번호는 화면 · 명령 기록에 남지 않는다. 결과의 ENC 두 줄만 yml 에 넣는다
#
# 실행 (저장소 맨 위에서):
#   powershell -ExecutionPolicy Bypass -File scripts\make-enc.ps1
#
# @author shinm
# @since 2026-11-04
# ======================================================================
# DATE              AUTHOR             NOTE
# ----------------------------------------------------------------------
# 2026-11-04        shinm              최초 생성
# ======================================================================

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent

# java — JAVA_HOME 이 있으면 그 java, 없으면 PATH 의 java
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }

# jasypt jar — Gradle 이 받아 둔 것을 쓴다 (없으면 gradlew dependencies 한 번)
$jasyptDir = Join-Path $env:USERPROFILE '.gradle\caches\modules-2\files-2.1\org.jasypt\jasypt'
$jar = Get-ChildItem $jasyptDir -Recurse -Filter 'jasypt-*.jar' -ErrorAction SilentlyContinue |
       Where-Object { $_.Name -notmatch 'sources|javadoc' } |
       Sort-Object Name | Select-Object -Last 1 -ExpandProperty FullName
if (-not $jar) { "jasypt jar 가 없습니다. 저장소에서 .\gradlew.bat dependencies 를 한 번 돌리세요"; return }

# 마스터 키 — 환경변수 → IntelliJ 실행 설정
$key = $env:JASYPT_ENCRYPTOR_PASSWORD
$from = '환경변수'
if (-not $key) {
    $ws = Join-Path $root '.idea\workspace.xml'
    if (Test-Path $ws) {
        $x = [xml](Get-Content $ws -Raw -Encoding UTF8)
        $key = ($x.SelectNodes("//env[@name='JASYPT_ENCRYPTOR_PASSWORD']") | Select-Object -First 1).value
        $from = 'IntelliJ 실행 설정'
    }
}
if (-not $key) { "마스터 키를 못 찾았습니다. IntelliJ 실행 설정이나 환경변수 JASYPT_ENCRYPTOR_PASSWORD 에 넣으세요"; return }
"마스터 키: $from 에서 가져옴 ($($key.Length) 글자)"

function Ask($label) {
    $s = Read-Host $label -AsSecureString
    $b = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($b) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($b) }
}

# 앱 설정(JasyptConfig)과 같은 알고리즘. 반복 1000 · 랜덤 salt 는 CLI 기본값과 같다
function Enc($plain) {
    $out = & $java -cp $jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI "input=$plain" "password=$key" "algorithm=PBEWithMD5AndDES" 2>&1 | Out-String
    if ($out -match '----OUTPUT-+\s+(\S+)') { return "ENC($($Matches[1]))" }
    return '실패 — 키에 한글 · 공백이 있는지 확인'
}

$dev  = Ask '개발 DB 비밀번호 (honeysalt_local)'
$prod = Ask '운영 DB 비밀번호 (honeysalt_prod)'
''
'개발: ' + (Enc $dev)
'운영: ' + (Enc $prod)
''
'application-local.yml · application-prod.yml 의 password 에 넣는다'
Remove-Variable key, dev, prod
