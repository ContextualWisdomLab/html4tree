## 2024-06-21 - 루프 내 정규식 컴파일
**학습:** Kotlin에서 무시 파일을 처리할 때 파일 반복 루프 내에서 정규식(`.toRegex()`)을 컴파일하는 것은 O(N * M)의 심각한 성능 병목을 일으킵니다 (N 파일 * M 규칙).
**조치:** 불필요한 정규식 재컴파일을 피하기 위해 항상 파일 반복 루프 외부에서 문자열 규칙을 컴파일된 `Regex` 객체로 매핑합니다 (O(M) 컴파일).

## 2024-05-24 - 루프 내 할당 핫 패스
**학습:** 디렉토리 항목을 렌더링할 때 반복적인 문자열 연결과 리스트 기반 제외 조회를 사용하면 대규모 디렉토리에서 불필요한 할당 및 조회 비용이 발생합니다.
**조치:** 항목 렌더링에 `StringBuilder`를 사용하고 제외된 파일 이름에 대해 `Set`을 사용합니다.

## 2024-07-26 - 중간 문자열 할당
**학습:** Kotlin에서 문자열에 연결된 `.replace()` 호출 (예: HTML 이스케이프)은 각 단계에서 중간 문자열을 할당하여 요소가 많은 핫 패스에서 성능 및 가비지 컬렉션에 큰 영향을 미칩니다.
**조치:** 연결된 `.replace()` 호출을 문자를 한 번만 반복하는 단일 패스 루프로 바꾸고, 변환된 출력을 추가하기 위해 `StringBuilder`를 지연 초기화합니다.

## 2024-07-08 - URL 인코딩 문자열 할당 병목
**학습:** 핫 패스 루프 내에서 예약된 바이트당 최대 3개의 문자열을 할당하는 `byte.toString(16).padStart(2, '0').toUpperCase()`는 상당한 GC 압력을 유발합니다. 이는 디렉토리 크롤러에서 대규모 문자열이나 수많은 파일을 처리할 때 Kotlin에서 흔히 볼 수 있는 위험한 안티 패턴입니다.
**조치:** 중간 문자열 생성을 완전히 피하기 위해 포맷된 16진수 출력을 작성할 때 연결된 문자열 연산을 직접 문자 매핑 및 비트 연산(`ushr`, `and`)으로 바꿉니다. 10 미만 및 9 초과의 16진수 값을 모두 포괄하는 테스트 입력을 통해 100% 브랜치 커버리지를 보장합니다.

## 2026-07-10 - 저렴한 메모리 내 검사 전 비싼 OS stat 호출
**학습:** Kotlin/Java에서 `java.nio.file.Files`를 통해 파일 속성 (예: `isDirectory` 또는 `isSymbolicLink`)을 확인하려면 `Path` 객체를 할당해야 하며 비싼 네이티브 OS stat 호출을 수행합니다. 파일 목록을 처리할 때 파일 시스템을 건드리는 메서드를 호출하기 전에 제외 목록 (저렴한 메모리 내 문자열 연산 사용)과 비교하여 파일 시스템 검사를 단축합니다.
**조치:** `Files.isDirectory` 및 `Files.isSymbolicLink`를 호출하기 전에 `exclude` 세트를 확인하도록 조건문을 재배열했습니다.


## 2026-07-12 - 이중 루프 내 패턴 매칭 조기 종료 (Short-Circuit)
**학습:** 무시할 파일(ignore patterns)을 확인할 때, 각 파일에 대해 모든 패턴을 순회(`forEach`)하는 것은 비효율적입니다. 파일이 하나의 패턴에 매칭되어 제외 목록에 추가되면 나머지 패턴을 확인할 필요가 없습니다. 이를 조기 종료(Short-circuit)하지 않으면 불필요한 O(N * M) 정규식/패턴 매칭 평가가 발생합니다.
**조치:** 무시 목록 평가 등 조건을 만족할 때 더 이상 확인이 필요 없는 경우에는 `forEach` 대신 일반 `for` 루프와 `break`를 사용하거나 `any`를 활용하여 연산을 단축합니다.

## 2024-07-28 - 디렉토리 목록 불필요한 정렬 오버헤드
**학습:** 디렉토리 목록(`list()` 또는 `listFiles()`)을 단순히 필터링하여 `Set`에 추가하는 경우처럼 특정 순서가 필요하지 않은 작업에서 `.sorted()`를 호출하면 불필요한 O(N log N) 오버헤드가 발생합니다.
**조치:** `Set`과 같은 순서에 무관한 자료구조에 요소를 추가하기 위한 필터링 작업에서는 디렉토리 목록에서 `.sorted()` 호출을 제거하여 성능을 최적화합니다.

## 2024-05-18 - [디렉토리 목록 캐싱을 통한 I/O 오버헤드 최적화]
**Learning:** `process_dir` 및 `process_ignore_file`과 같은 함수에서 동일한 디렉토리에 대해 `listFiles()` 또는 `list()`를 반복적으로 호출하면, 파일 시스템 I/O로 인한 불필요한 성능 저하가 발생합니다.
**Action:** 디렉토리를 순회할 때 상위 루프에서 `listFiles()`를 한 번만 호출하여 캐싱한 후, 결과를 인자로 전달(예: `dirFiles` 배열)하여 중복된 파일 시스템 호출을 제거해야 합니다.

## 2024-08-01 - URL 인코딩 빌더 지연 생성
**학습:** URL 인코딩이 필요 없는 안전한 경로 문자열에서도 항상 `StringBuilder`를 생성하면 hot path에서 불필요한 할당이 발생합니다.
**조치:** 예약 바이트를 처음 만났을 때만 `StringBuilder`를 만들고, 그 전까지는 원본 문자열을 그대로 반환하는 지연 생성 패턴을 사용합니다.
## $(date +%Y-%m-%d) - Optimize OS stat calls in file listing
**Learning:** Replaced three separate OS stat calls (`Files.isDirectory(it.toPath(), LinkOption.NOFOLLOW_LINKS)`, `!it.isDirectory()`, and `!Files.isSymbolicLink(it.toPath())`) with a single `Files.readAttributes` call. The original code caused significant I/O overhead. This reduces file metadata fetching time significantly.
**Action:** Always consider using `Files.readAttributes` to fetch multiple file attributes at once rather than calling separate boolean checks like `isDirectory` or `isSymbolicLink` on individual files when iterating directories.
## 2025-01-24 - 단일 readAttributes 호출로 파일 속성 조회 최적화
**학습:** `isDirectory`, `!it.isDirectory()`, `isSymbolicLink` 3개의 개별적인 파일 시스템 I/O 호출을 수행하면 성능 저하가 큽니다. 이를 단일 `Files.readAttributes` 호출로 변경하여 메타데이터를 한 번에 조회함으로써 I/O 오버헤드를 대폭 줄일 수 있음을 확인했습니다.
**조치:** 디렉토리 순회 시 파일의 여러 속성을 확인할 때는 개별적인 stat 호출보다 `Files.readAttributes`를 사용하여 필요한 모든 속성을 한 번에 가져오는 방식을 우선적으로 고려해야 합니다.
## 2025-01-24 - 단일 readAttributes 호출로 파일 속성 조회 최적화
**학습:** `isDirectory`, `!it.isDirectory()`, `isSymbolicLink` 3개의 개별적인 파일 시스템 I/O 호출을 수행하면 성능 저하가 큽니다. 이를 단일 `Files.readAttributes` 호출로 변경하여 메타데이터를 한 번에 조회함으로써 I/O 오버헤드를 대폭 줄일 수 있음을 확인했습니다.
**조치:** 디렉토리 순회 시 파일의 여러 속성을 확인할 때는 개별적인 stat 호출보다 `Files.readAttributes`를 사용하여 필요한 모든 속성을 한 번에 가져오는 방식을 우선적으로 고려해야 합니다.
## 2026-07-17 - [중복 할당 및 해시 연산을 방지하기 위한 정적 자원 추출]
**학습:** 여러 디렉토리를 처리하는 루프 내에서 큰 상수 문자열(정적 CSS 등)과 결정론적 연산(SHA-256 해시 등)을 유지하면 디렉토리 반복마다 불필요한 메모리 할당과 CPU 오버헤드가 발생합니다.
**조치:** 이를 `const val` 및 `@JvmField`를 사용하여 `private object`로 추출하여 디렉토리 반복당 O(N) 할당 오버헤드를 방지하는 동시에, 컴파일러가 생성하는 암시적 getter를 피하여 JaCoCo 테스트 커버리지를 100%로 유지합니다.
