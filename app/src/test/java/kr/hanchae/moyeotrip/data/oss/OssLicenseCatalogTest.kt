package kr.hanchae.moyeotrip.data.oss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OssLicenseCatalogTest {
    @Test
    fun parsesNameVersionLicenseAndOptionalFields() {
        val items = OssLicenseCatalog.parse(
            """
            {
              "items": [
                {
                  "name": "Sentry Android",
                  "version": "8.43.0",
                  "license": "MIT",
                  "licenseTextId": "MIT",
                  "url": "https://github.com/getsentry/sentry-java"
                },
                {
                  "name": "Kakao Maps SDK for Android",
                  "version": "2.15.1",
                  "license": "카카오 지도 SDK 이용약관",
                  "url": "https://apis.map.kakao.com",
                  "note": "오픈소스 라이선스가 아닙니다."
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(2, items.size)
        assertEquals("Sentry Android", items[0].name)
        assertEquals("8.43.0", items[0].version)
        assertEquals("MIT", items[0].license)
        assertEquals("MIT", items[0].licenseTextId)
        assertNull(items[0].note)
        assertNull(items[1].licenseTextId)
        assertEquals("오픈소스 라이선스가 아닙니다.", items[1].note)
    }

    @Test
    fun buildsRouteSlugsFromNamesWithSpaces() {
        val items = OssLicenseCatalog.parse(
            """{"items":[{"name":"AndroidX Core KTX","version":"1.18.0","license":"Apache-2.0","url":"u"}]}"""
        )

        assertEquals("androidx-core-ktx", items.single().slug)
    }

    @Test
    fun skipsEntriesWithoutNamesAndSurvivesBrokenJson() {
        assertEquals(
            emptyList<OssLicense>(),
            OssLicenseCatalog.parse("""{"items":[{"version":"1.0"}]}""")
        )
        assertEquals(emptyList<OssLicense>(), OssLicenseCatalog.parse("nope"))
        assertEquals(emptyList<OssLicense>(), OssLicenseCatalog.parse(""))
    }

    @Test
    fun embeddedAndroidCatalogHasTwelveEntriesWithUniqueSlugs() {
        val items = OssLicenseCatalog.items

        assertEquals(12, items.size)
        assertEquals(items.size, items.map { it.slug }.toSet().size)
        assertTrue(items.all { it.name.isNotBlank() && it.version.isNotBlank() && it.url.isNotBlank() })
    }

    @Test
    fun doesNotClaimApacheForGoogleSdkLicensedDependencies() {
        val firebaseAuth = OssLicenseCatalog.items.single { it.name == "Firebase Authentication" }

        assertEquals("Android Software Development Kit License", firebaseAuth.license)
        assertNull(firebaseAuth.licenseTextId)
        assertNull(OssLicenseCatalog.licenseText(firebaseAuth))
    }

    @Test
    fun readsEmbeddedLicenseTextsOffline() {
        val apache = OssLicenseCatalog.items.first { it.licenseTextId == "Apache-2.0" }
        val mit = OssLicenseCatalog.items.first { it.licenseTextId == "MIT" }

        assertNotNull(OssLicenseCatalog.licenseText(apache))
        assertTrue(OssLicenseCatalog.licenseText(apache)!!.contains("Apache License"))
        assertTrue(OssLicenseCatalog.licenseText(mit)!!.contains("Permission is hereby granted"))
    }

    @Test
    fun selfDistributedSdkHasNoLicenseTextToShow() {
        val kakaoMaps = OssLicenseCatalog.items.single { it.name == "Kakao Maps SDK for Android" }

        assertNull(OssLicenseCatalog.licenseText(kakaoMaps))
        assertNotNull(kakaoMaps.note)
    }

    @Test
    fun findFallsBackToTheFirstEntryForUnknownSlugs() {
        assertEquals(OssLicenseCatalog.items.first(), OssLicenseCatalog.find("nope"))
        assertEquals(
            OssLicenseCatalog.items[1],
            OssLicenseCatalog.find(OssLicenseCatalog.items[1].slug)
        )
    }
}
