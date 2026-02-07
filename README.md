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

## Публикация в GitHub Packages

1. Создайте Personal Access Token c правами `write:packages` (и `read:packages` при необходимости).
2. Передайте параметры через переменные окружения:
   ```bash
   export GITHUB_OWNER=<owner>
   export GITHUB_REPO=<repo>
   export GITHUB_ACTOR=<github_username>
   export GITHUB_TOKEN=<github_pat>
   ```
3. Выполните публикацию:
   ```bash
   ./gradlew publishAllPublicationsToGitHubPackagesRepository
   ```

## CI/CD через GitHub Actions

В репозитории добавлен workflow `.github/workflows/publish.yml` с двумя режимами:

1. Авто-публикация по тегу:
   - создайте тег вида `v1.2.3` и отправьте его в репозиторий;
   - workflow соберёт проект и опубликует версию `1.2.3` в GitHub Packages.
2. Полуручной запуск:
   - запустите workflow вручную (`Run workflow`);
   - укажите `version` (опционально) и флаг `publish`;
   - если `publish=false`, выполнится только сборка и тесты (без публикации).

Для публикации workflow использует встроенный `GITHUB_TOKEN`.
## Альтернатива через `~/.gradle/gradle.properties`

Можно положить секреты в `~/.gradle/gradle.properties` вместо переменных:

```properties
githubOwner=<owner>
githubRepo=<repo>
githubUser=<github_username>
githubToken=<github_pat>

```
