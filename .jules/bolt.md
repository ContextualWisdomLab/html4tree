## 2024-09-05 - [성능 최적화: escapeHtml 배열 기반 조회로 변경]
**Learning:** Kotlin에서 문자를 매핑할 때, 핫 패스(예: HTML 이스케이프)에서 `when` 조건 분기 테이블을 사용하는 것보다 직접 배열 기반 조회(예: `Array<String?>(128)`)를 사용하는 것이 훨씬 더 빠릅니다. `ArrayIndexOutOfBoundsException`을 방지하기 위해 배열 접근 전에 반드시 범위 검사(예: `cInt < 128`)를 수행해야 합니다. 그리고 JaCoCo 테스트 커버리지를 유지하기 위해 속성을 `private object` 안에 넣고 `@JvmField` 어노테이션을 사용하여 암묵적 getter 생성을 방지해야 합니다.
**Action:** 빈번하게 호출되는 문자 변환이나 이스케이프 로직을 최적화할 때는 배열 조회를 사용하고, 커버리지 유지를 위해 `@JvmField`를 적극 활용합니다.

## 2024-09-05 - [성능 최적화: escapeHtml 배열 기반 조회로 변경]
**Learning:** Kotlin에서 문자를 매핑할 때, 핫 패스(예: HTML 이스케이프)에서 `when` 조건 분기 테이블을 사용하는 것보다 직접 배열 기반 조회(예: `Array<String?>(128)`)를 사용하는 것이 훨씬 더 빠릅니다. `ArrayIndexOutOfBoundsException`을 방지하기 위해 배열 접근 전에 반드시 범위 검사(예: `cInt < 128`)를 수행해야 합니다. 그리고 JaCoCo 테스트 커버리지를 유지하기 위해 속성을 `private object` 안에 넣고 `@JvmField` 어노테이션을 사용하여 암묵적 getter 생성을 방지해야 합니다.
**Action:** 빈번하게 호출되는 문자 변환이나 이스케이프 로직을 최적화할 때는 배열 조회를 사용하고, 커버리지 유지를 위해 `@JvmField`를 적극 활용합니다.
