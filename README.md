# Удаленное хранилище файлов. 

Используемые технологии:
Java 8, Swing, IO, NIO, JDBS, multi-threads, SQLite.
 
В проекте реализуется удаленное хранилище файлов.
Возможна передача, получение, удаление, синхронизация, просмотр списка файлов в хранилище.
Также возможно создание, переименование, удаление папок.


# Сборка проекта.
Распаковать проект, перейти в папку "mySimpleNetFileStore", выполнить команду: "mvn clean package".
Для запуска сервера выполнить команду: "java -jar ./server/target/server-1.0-jar-with-dependencies.jar"
Для запуска клиента выполнить команду: "java -jar ./client/target/client-1.0-jar-with-dependencies.jar".

При первом запуске сервера в папке "./server/target/" будет создана новая база данных.