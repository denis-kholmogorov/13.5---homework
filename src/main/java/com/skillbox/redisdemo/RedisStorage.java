package com.skillbox.redisdemo;

import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;

import java.util.Date;
import java.util.Random;

import static java.lang.System.out;
import static java.lang.System.setOut;

public class RedisStorage {

    // Объект для работы с Redis
    private RedissonClient redisson;

    // Объект для работы с ключами
    private RKeys rKeys;

    // Объект для работы с Sorted Set'ом
    private RScoredSortedSet<String> onlineUsers;

    // Количество users
    private int countsUsers;

    private final static String KEY = "USERS";

    private double getTs() {
        return new Date().getTime() / 1000;
    }

    // Пример вывода всех ключей
    public void listKeys() {
        Iterable<String> keys = rKeys.getKeys();
        for(String key: keys) {
            out.println("KEY: " + key + ", type:" + rKeys.getType(key));
        }
    }

    void init() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        try {
            redisson = Redisson.create(config);
        } catch (RedisConnectionException Exc) {
            out.println("Не удалось подключиться к Redis");
            out.println(Exc.getMessage());
        }
        rKeys = redisson.getKeys();
        onlineUsers = redisson.getScoredSortedSet(KEY);
        rKeys.delete(KEY);
    }

    void shutdown() {
        redisson.shutdown();
    }

    // Фиксирует посещение пользователем страницы
    void logPageVisit(int user_id)
    {
        //ZADD ONLINE_USERS
        onlineUsers.add(user_id, String.valueOf(user_id));
    }

    void read() throws InterruptedException {
        countsUsers = onlineUsers.size();
        out.println(countsUsers);
        for(;;){
            String a = onlineUsers.takeFirst();
            out.println("- На главной странице показываем пользователя " + a);
            int rand = (int)(Math.random() * 200) + 1;
            if(rand <= 20 && rand != Integer.parseInt(a))
            {
                out.println("> Пользователь \"" + rand + "\" оплатил подписку ");
               // out.print(onlineUsers.getScore(String.valueOf(rand)) + " ------ ");
                onlineUsers.addScore(String.valueOf(rand), 1-onlineUsers.getScore(String.valueOf(rand)));
              //  out.println (onlineUsers.getScore(String.valueOf(rand)));

                for(int j = rand; j <=onlineUsers.size(); j++ )
                {
                    onlineUsers.addScore(String.valueOf(j), -1.0);
                }
            }
            else{
                for (String user : onlineUsers)
                {
                    onlineUsers.addScore(user, -1.0);
                }
            }

            Thread.sleep(250);

            onlineUsers.add(20.0, a);
        }
    }
}
