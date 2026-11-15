# providers-registry

Реестр HTTP-клиентов поставщиков. По идентификатору поставщика и номеру телефона сообщает, доступен ли номер.
Веб-эндпоинты отсутствуют.  
рест эндпоинты отсутствуют, так что это библиотеки для подключения в другой проект.

В этом readme я описал конфигурацию проекта, как проверить и оставил некоторые заметки.

Стэк: Java 17, Spring Boot 4.1.2, Gradle.

Тесты из задания в `ProviderClientTest`. Остальные тесты я добавил от себя, возможно немного увлекся с ними.

## Что тут происходит

Наружу торчат 3 метода:

- **isPhoneAvailable (Long providerId, String phone)** - проверка доступности номера по собственно номеру и id
  поставщика.
  Метод для выполнения основной функции приложения. Возвращает true, false или бросает исключение.
- **replaceProviders (Collection<Provider>)** - замена списка поставщиков на свой.
- **reload ()** - перечитывание списка из источника (файл или статика) и замена поставщиков на него.

Поставщиков можно передать готовым списком, используя статический источник поставщиков:

```java
ProvidersRegistry registry = new ProvidersRegistry(
    new StaticProviderSource(List.of(new Provider(1L, "https://api.example", "key"))),
    new ProviderClientFactory(RestClient.builder())
);
registry.reload();

boolean available = registry.isPhoneAvailable(1L, "+79991234567");
```

Или прочитать из JSON-файла (`JsonFileProviderSource`).

## Конфигурация

В `application.yml` задаётся путь к файлу поставщиков:

```yaml
providers-registry:
  providers-file: providers.json
```
`providers.json` ДОБАВЛЕН В .gitignore, ПО УМОЛЧАНИЮ ЕГО НЕТ В ПРОЕКТЕ, ПРИ ЗАПУСКЕ УПАДЕТ ОШИБКА.

Формат файла с поставщиками:

```json
[
  {
    "id": 1,
    "baseUrl": "https://api.provider-a.example",
    "apiKey": "key-a"
  }
]
```

Пример закоммичен: `providers.example.json`.  
Если файла из конфига нет или JSON некорректен, приложение должно упасть на старте.

## Допущения

- **Передача API-ключа.** Заголовок написал `Authorization: Bearer {apiKey}`. Можно посмотреть в`ProviderClientFactory`.
- **«Набор поставщиков можно заменить целиком»** понял как возможность в любой момент вызвать `replaceProviders`
  в классе `ProvidersRegistry` со своим массивом.
  Замену постарался сделать синхронизированной и атомарной, на случай работы с многопоточкой, возможно overkill.
- **Номера телефонов**. Для простоты я оставил просто строкой, никаких валидаций или масок нет.