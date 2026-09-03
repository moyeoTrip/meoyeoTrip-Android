package kr.hanchae.moyeotrip.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 빈 상태 문구 정본 (`docs/alignment/NO-MOCK-CANON.md` §2).
 *
 * 웹·iOS 와 **글자 그대로** 같은 문자열이라 화면마다 새로 쓰지 않는다.
 * 여기 없는 자리는 문구를 지어내지 말고 정본 문서에 먼저 추가한다.
 */
object MoyeoEmptyText {
    const val SIGN_IN_EXPLORE = "로그인하면 모집 중인 모임을 볼 수 있어요."
    const val SIGN_IN_SEARCH = "로그인하면 모임을 검색할 수 있어요."
    const val NO_ROOMS = "지금 모집 중인 모임이 없어요."
    const val NO_SEARCH_RESULTS = "검색 결과가 없어요"
    const val NO_RECENT_SEARCHES = "최근 검색어가 없어요."
    const val NO_NOTICES = "아직 등록된 공지가 없어요."
    const val NO_COMMENTS = "아직 댓글이 없어요."
    const val NO_FEEDS = "아직 올라온 피드가 없어요."
    /** 26-1 내 피드가 0건일 때. 남의 피드가 없는 것(`NO_FEEDS`)과 다른 상황이라 따로 둔다 —
     *  여기서는 **내가** 쓰면 채워지므로 무엇을 하면 되는지까지 알려준다. */
    const val NO_MY_FEEDS = "아직 쓴 피드가 없어요.\n다녀온 여행을 피드로 남겨보세요."
    /** 25-1 카드 뒷면에 함께한 여행도 받은 평가도 없을 때. 뒷면이 통째로 비어 있었다. */
    const val NO_COMPANION_HISTORY = "아직 함께한 여행과 평가가 없어요.\n함께 여행하면 기록과 한줄평이 여기 쌓여요."
    /** 25-1 「다른 여행자들이 남긴 평가」가 0건일 때. 제목만 남고 아래가 비어 휑했다 —
     *  무엇을 하면 채워지는지까지 적는다. */
    const val NO_RECEIVED_REVIEWS = "아직 받은 한줄평이 없어요.\n함께 여행하면 서로 한줄평을 남길 수 있어요."
    const val NO_JOINED_ROOMS = "참여 중인 모임이 없어요."
    const val NO_NOTIFICATIONS = "새 알림이 없어요."

    /** 36 오프라인(캐시 있음) 홈의 "저장해둔 코스" 섹션이 0건일 때. iOS 와 같은 문구다. */
    const val NO_SAVED_COURSES = "저장해둔 코스가 없어요."

    /** 13-1 내보내기 안내 · 13-2 내 강퇴 이력이 0건일 때. 정본 §2 표의 글자 그대로다. */
    const val NO_KICK_HISTORIES = "내보내진 모임이 없어요."
    const val LOADING = "불러오는 중이에요…"
    const val FAILED = "불러오지 못했어요."
    const val RETRY = "다시 시도"
}

/**
 * 목록 자리에 그리는 빈 상태 한 덩어리.
 * [onRetry] 를 주면 §2 의 "실패 + 다시 시도" 형태가 된다.
 */
@Composable
fun MoyeoEmptyState(
    text: String,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp)
            .then(if (testTag == null) Modifier else Modifier.testTag(testTag)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text(MoyeoEmptyText.RETRY, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/**
 * 서버에서 목록 하나를 읽어오는 동안의 세 가지 상태.
 * 화면마다 `null = 로딩` 같은 규칙을 따로 만들지 않게 값으로 들고 다닌다.
 */
sealed interface ServerListState<out T> {
    data object Loading : ServerListState<Nothing>

    data object Failed : ServerListState<Nothing>

    data class Loaded<T>(val items: List<T>) : ServerListState<T>
}

/**
 * 뒤에서 돌린 갱신 결과를 반영한다.
 *
 * 갱신에 실패했는데 **이미 받아 둔 목록이 있으면 그대로 둔다** — 보고 있던 내용을 오류로 덮으면
 * 사용자가 아무것도 못 하게 된다(`TAB-STATE-CANON.md` R2). 가진 게 없을 때만 실패를 보여준다.
 */
fun <T> ServerListState<T>.afterReload(result: Result<List<T>>): ServerListState<T> = result.fold(
    onSuccess = { ServerListState.Loaded(it) },
    onFailure = { if (this is ServerListState.Loaded) this else ServerListState.Failed }
)
