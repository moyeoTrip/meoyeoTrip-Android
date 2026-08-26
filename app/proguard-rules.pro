# R8 규칙 (release 빌드에서만 적용된다)
#
# 적용 조건은 app/build.gradle.kts 의 release 블록:
#   isMinifyEnabled = true      코드 난독화 + 미사용 코드 제거
#   isShrinkResources = true    미사용 리소스 제거
#
# 매핑 파일: app/build/outputs/mapping/release/mapping.txt
#   -> Play Console 과 Sentry 에 반드시 업로드해야 스택트레이스를 읽을 수 있다.

# ── 크래시 로그 가독성 ─────────────────────────────────────────────
# 클래스·메서드 이름은 난독화하되 줄 번호는 남긴다.
# 원본 파일명은 SourceFile 로 덮어써서 노출하지 않는다.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── 카카오맵 SDK ───────────────────────────────────────────────────
# libK3fAndroid.so 가 JNI FindClass 로 com/kakao/vectormap/** 을 이름으로 찾는다.
# (LatLng, CameraPosition, LabelOptions, MapViewHolder 등 31개 확인)
# 난독화되면 지도 초기화 시점에 런타임 크래시가 난다. AAR 에 consumer 규칙이 없어 직접 유지한다.
-keep class com.kakao.vectormap.** { *; }
-keep interface com.kakao.vectormap.** { *; }

# ── Retrofit (카카오 SDK 가 내부적으로 Retrofit 2.9.0 을 쓴다) ──────
# R8 full mode 는 제네릭 시그니처를 지운다. 그러면 Retrofit 이 Call<T> 의 T 를 못 읽고
#   "Call return type must be parameterized as Call<Foo>" 로 앱이 시작 즉시 죽는다.
#   (실제 재현: AppLifecycleObserver -> UserApi.checkAccessToken)
# Retrofit 2.10 부터는 아래 규칙이 라이브러리에 동봉되지만 2.9.0 에는 없어 직접 넣는다.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# @GET/@POST 등이 붙은 서비스 인터페이스의 반환 타입을 유지한다.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ── 카카오 로그인 SDK ──────────────────────────────────────────────
# 응답 모델은 kotlinx.serialization 으로 역직렬화된다. 필드명이 곧 JSON 키다.
-keep class com.kakao.sdk.**.model.** { <fields>; }

# ── 앱 자체 코드 ───────────────────────────────────────────────────
# 네트워크 응답은 org.json 으로 직접 파싱하고 리플렉션을 쓰지 않으므로
# kr.hanchae.moyeotrip 패키지에는 별도 keep 규칙이 필요 없다.
# (Activity/Service/Receiver 는 매니페스트 참조로 R8 이 자동 유지한다)
