package kr.hanchae.moyeotrip.ui

/**
 * 문의 창구. **네 표면이 같은 값을 쓴다** — 여기만 고치면 되도록 한 곳에 모은다.
 *
 * 웹      `moyeoTrip-Web/src/screens-extra2.jsx` 의 `MOYEO_CONTACT`
 * iOS     `MoyeoTrip/MoyeoContact.swift`
 * 기획    `모여트립 in 경북/screens-extra2.jsx`
 *
 * 고객센터는 존재하지 않는 개념이다(정본 `changeLog14`). 문의는 두 갈래뿐이다 —
 * 버그·기능 제안은 GitHub 이슈, 그 밖은 이메일.
 */
object MoyeoContact {
    const val ISSUES_URL = "https://github.com/moyeoTrip/moyeoTrip-BACKEND/issues"
    const val EMAIL = "konempty.dev@gmail.com"
    const val MAILTO_URL = "mailto:$EMAIL"

    const val ISSUES_LABEL = "GitHub 이슈로 문의"
    const val EMAIL_LABEL = "이메일로 문의"
    const val DIALOG_BODY = "버그 제보나 기능 제안은 GitHub 이슈로, 그 밖의 문의는 이메일로 보내주세요."
}
