# NightMarket

![Paper](https://img.shields.io/badge/Paper-1.21.11-22c55e?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-f97316?style=for-the-badge&logo=openjdk)
![Version](https://img.shields.io/badge/version-1.0.0-111827?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-2563eb?style=for-the-badge)

Ночной торговец появляется рядом с игроками и продаёт редкие предметы.

## Версия

NightMarket 1.0.0

Paper 1.21.11  
API 1.21.11-R0.1-SNAPSHOT  
Java 21

## Команды

`/nightmarket spawn` - вызвать торговца
`/nightmarket despawn` - убрать торговца
`/nightmarket reload` - перезагрузить конфиг
Алиас: `/nmarket`

## Permission

`nightmarket.admin`
По умолчанию доступно op.

## Функции

- автоматически появляется ночью;
- создаёт Wandering Trader с кастомными сделками;
- сам удаляет прошлого торговца;
- можно вызвать вручную.

## Сборка

```bash
./gradlew build
```

Готовый `.jar` будет в `build/libs/`.
