package com.skillbox.redisdemo;

public class RedisTest {

    // Запуск докер-контейнера:
    // docker run --rm --name skill-redis -p 127.0.0.1:6379:6379/tcp -d redis

    public static void main(String[] args) throws InterruptedException {

        RedisStorage redis = new RedisStorage();
        redis.init();

        // Создаем список из 20 пользователей
        for(int i = 1; i <=20; i++){
            redis.logPageVisit(i);
        }
        redis.read();
        redis.shutdown();
    }
}
