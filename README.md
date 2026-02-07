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

## Публикация в GitLab Package Registry

1. Узнайте `project_id` проекта в GitLab.
2. Создайте токен (Personal Access Token или Deploy Token) c правом `write_package_registry`.
3. Передайте параметры:
   ```bash
   export GITLAB_PROJECT_ID=<project_id>
   export GITLAB_USER=<gitlab_username_or_token_name>
   export GITLAB_TOKEN=<gitlab_token>
   ```
4. Выполните публикацию:
   ```bash
   ./gradlew publishAllPublicationsToGitLabPackagesRepository
   ```

## Альтернатива через `~/.gradle/gradle.properties`

Можно положить секреты в `~/.gradle/gradle.properties` вместо переменных:

```properties
githubOwner=<owner>
githubRepo=<repo>
githubUser=<github_username>
githubToken=<github_pat>

gitlabProjectId=<project_id>
gitlabUser=<gitlab_username_or_token_name>
gitlabToken=<gitlab_token>
```
