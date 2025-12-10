# Exception Handler Starter

Глобальный обработчик исключений для Spring Boot-приложений, оформленный как библиотека.

> Добавляйте новые коды через PR, дополняя таблицу.

## Сборка

```bash
./gradlew clean build
```

## Публикация в локальный Maven

Используйте стандартную задачу Gradle:

```bash
./gradlew publishToMavenLocal
```

Артефакт появится в `~/.m2/repository/ru/vassuv/exceptionhandler/0.0.1-SNAPSHOT/` и будет доступен для локальных проектов.

## Публикация в удалённый репозиторий

1. Настройте репозиторий в `build.gradle.kts`:
   ```kotlin
   publishing {
       repositories {
           maven {
               name = "myRepo"
               url = uri("https://example.com/maven")
               credentials {
                   username = findProperty("repoUser") as String? ?: System.getenv("REPO_USER")
                   password = findProperty("repoPassword") as String? ?: System.getenv("REPO_PASSWORD")
               }
           }
       }
   }
   ```
2. Запустите публикацию:
   ```bash
   ./gradlew publish
   ```

Перед пушем убедитесь, что переменные/gradle.properties содержат креденшалы для удалённого репозитория.
