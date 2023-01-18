package main;

import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static java.lang.System.out;

public class Main {

        // Jedis jedis = new Jedis("localhost", 6379);


        // Запуск докер-контейнера:
        // docker run --rm --name skill-redis -p 127.0.0.1:6379:6379/tcp -d redis

        // Для теста будем считать неактивными пользователей, которые не заходили 3 секунды
        private static final int DELETE_SECONDS_AGO = 3;

        // Допустим пользователи делают 300 запросов к сайту в секунду
        private static final int RPS = 300;

        // И всего на сайт заходило 1000 различных пользователей
        private static final int USERS = 20;

        // Также мы добавим задержку между посещениями
        private static final int SLEEP = 1000; // 1 сек

        private static final SimpleDateFormat DF = new SimpleDateFormat("HH:mm:ss");

        private static void log(int UsersOnline) {
            String log = String.format("[%s] Пользователей онлайн: %d", DF.format(new Date()), UsersOnline);
            out.println(log);
        }

        public static void main(String[] args) throws InterruptedException {

            RedisStorage redis = new RedisStorage();
            redis.init();
            // Эмулируем 10 секунд работы сайта
            for(int seconds=0; seconds <= 10; seconds++) {
                // Выполним 300 запросов
                for(int request = 0; request <= RPS; request++) {
                    int n = 0;
                    for(int i = 1; i <= USERS; i++) {
                        int user_id = i;
                        int value =  new Random().nextInt(2 * USERS);
                        if (value > user_id && value <= USERS) {
                            out.println("Пользователь " + value + " оплатил услугу");
                            redis.logPageVisit(value);
                            n = value;
                        }
                        if(user_id != n) {
                            redis.logPageVisit(user_id);
                        }
                          Thread.sleep(SLEEP);
                    }
                }
                redis.deleteOldEntries(DELETE_SECONDS_AGO);
                int usersOnline = redis.calculateUsersNumber();
               // log(usersOnline);
            }
            redis.shutdown();
        }


}
