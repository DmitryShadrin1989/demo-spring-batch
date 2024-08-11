# Demo Spring Batch
### Демонстрационное приложение по работе с фреймворком Spring Batch

## Цель:
Продемонстрировать возможности Spring Batch, используя различные сценарии

## Технологии:
### Frameworks:
 - Spring Boot v3.1.2
 - Spring Batch v5.0.2

### Интерфейс:
 - Spring Shell v3.1.3 - библиотека для создания командных интерфейсов; 

### Используемые хранилища данных:
 - Mongo v7.0 - как целевое хранилище данных. Есть инициализация данных с помощью Mongock. Запускается в Docker;
 - Postgres v13 - как источник данных для миграции. Запускается в Docker;
 - CSV-файлы - как источник данных для миграции. Файлы лежат в дирректории resources. 

## Реализованные сценарии:
 - Миграция авторов (Author) и жанров (Genre) - используются доменные модели как для единственного источника данных БД Postgres так и для целевого хранилища. 
Связи идентификаторов хранятся в памяти приложения. Миграция происходит параллельно за счет использования Flow;
 - Миграция книг (Book) - используются доменные модели как для единственного источника данных БД Postgres так и для целевого хранилища. 
Связи идентификаторов храняться во временной таблице в БД Postgres. Временная таблица создается и удаляется с помощью TaskletStep.
Для записи в целевое хранилище и временную таблицу используется CompositeItemWriter. 
В целях демонстрации в Step добавлены listener-ы.
 - Миграция комментариев к книгам (Comment) - доменная модель используется только для записи в целевое хранилище. 
Источников данных 2а: БД Postgres и CSV-файл. Для чтения из БД используется JdbcCursorItemReader. Для чтения из CSV используется FlatFileItemReader. 
Добавлен CustomCompositeItemReader для объединения двух источников данных, сначала читаются данные из БД потом из CSV-файла.

## Полезные ссылки:
 - https://docs.spring.io/spring-batch/reference/index.html - документация
 - https://docs.spring.io/spring-batch/reference/scalability.html - многопоточная обработка (из документации)
 - https://spring.io/guides/gs/batch-processing - гайд по созданию сервиса
 - https://javainside.ru/primer-ispolzovaniya-spring-batch-3-0-chast-1/ - Spring Batch 3.0 – Часть 1: Пример использования
 - https://javainside.ru/spring-batch-3-0-chast-2-itemreader-itempocessor-itemwriter/ - Spring Batch 3.0 – Часть 2: Кастомный ItemReader, ItemPocessor и ItemWriter



#### Автор: Шадрин Дмитрий, Email: dmitry.shadrin1989@inbox.ru
