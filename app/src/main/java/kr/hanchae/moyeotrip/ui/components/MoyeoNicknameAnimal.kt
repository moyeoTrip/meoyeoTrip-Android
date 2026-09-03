package kr.hanchae.moyeotrip.ui.components

/**
 * 닉네임의 동물 → 이모지. **안드로이드의 정본 한 곳이다.**
 *
 * 서버 닉네임은 "형용사 동물 숫자" 형식이고(`POST /auth/nickname-candidates` 의 `animal`),
 * 프로필 이미지가 없을 때 그 동물을 아바타로 쓴다. 웹·iOS 도 같은 규칙을 쓴다.
 *
 * 이전에는 같은 표가 세 곳(`ProfileCardScreen`·`UserAvatar`·`AuthFlowScreen`)에 각각 있었고
 * 서로 달랐다 — 실서버가 주는 20종 중 프로필 카드는 10종만 덮어 참새·펭귄·판다·강아지·
 * 고슴도치 등이 `🐾` 로 떨어졌고, 아바타는 **미지의 동물을 `🐻`(곰)으로** 떨어뜨려
 * "따스한 기린 2334" 아바타에 곰이 떴다. 두루미도 `🕊️` 와 `🪽` 로 갈렸다.
 *
 * 실서버 표본(`POST /auth/nickname-candidates` 135회, 2026-08-29)에서 확인한 20종을 모두 덮는다.
 * `곰` 은 서버가 주지 않지만 화면기획 목데이터에 있어 남겨둔다.
 */
internal object MoyeoNicknameAnimal {
    /** 알 수 없는 동물. 발자국은 어떤 동물이든 어색하지 않다(곰 같은 특정 동물로 떨어뜨리지 않는다). */
    const val FALLBACK = "🐾"

    private val EMOJI_BY_ANIMAL = mapOf(
        "사슴" to "🦌",
        "고라니" to "🦌",
        "거북이" to "🐢",
        "토끼" to "🐰",
        "여우" to "🦊",
        "수달" to "🦦",
        "해달" to "🦦",
        "다람쥐" to "🐿️",
        "고양이" to "🐱",
        "강아지" to "🐶",
        "판다" to "🐼",
        "펭귄" to "🐧",
        "돌고래" to "🐬",
        "부엉이" to "🦉",
        "참새" to "🐦",
        "알파카" to "🦙",
        "코알라" to "🐨",
        "두루미" to "🪽",
        "고슴도치" to "🦔",
        "너구리" to "🦝",
        "기린" to "🦒",
        "곰" to "🐻"
    )

    /** 동물 이름 하나로 찾는다. 가입 플로우처럼 `animal` 을 따로 받는 곳에서 쓴다. */
    fun emojiForAnimal(animal: String?): String = EMOJI_BY_ANIMAL[animal?.trim()] ?: FALLBACK

    /**
     * 닉네임 전체에서 동물을 뽑아 찾는다.
     *
     * 끝이 숫자면 그 앞이 동물, 아니면 마지막 낱말이 동물이다 — 웹·iOS 와 같은 규칙이다.
     * 부분 문자열 비교는 쓰지 않는다(형용사에 동물 이름이 들어가면 엉뚱하게 걸린다).
     */
    fun emojiForNickname(nickname: String?): String {
        val words = nickname.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return FALLBACK
        val last = words.last()
        val animal = if (words.size >= 2 && last.all { it.isDigit() }) words[words.size - 2] else last
        return emojiForAnimal(animal)
    }
}
