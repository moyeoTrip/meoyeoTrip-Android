# Sentry setup

Sentry is disabled when `SENTRY_DSN` is empty. No DSN or auth token is committed.

For local builds, add these values to the ignored `local.properties` file:

```properties
SENTRY_DSN=https://PUBLIC_KEY@SENTRY_HOST/PROJECT_ID
SENTRY_ENVIRONMENT=development
```

For CI/release builds, expose the same names as Gradle properties or environment variables. `SENTRY_AUTH_TOKEN` is not needed by the runtime SDK and must not be placed in the app. If a future release pipeline uploads ProGuard mappings, keep that token only in CI secrets.

The runtime configuration disables default PII. Performance tracing is disabled for debug builds and sampled at 10% for non-debug builds.
