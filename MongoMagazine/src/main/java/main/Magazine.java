package main;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import org.bson.BsonDocument;
import org.bson.Document;

import javax.print.Doc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Consumer;

public class Magazine {

    private int price;
    private String nameOfGood;
    private String nameOfMagazine;
    private static String ADDMAGAZINE = "ДОБАВИТЬ_МАГАЗИН";
    private static String ADDGOODS = "ДОБАВИТЬ_ТОВАР";
    private static String PUTGOODS = "ВЫСТАВИТЬ_ТОВАР";
    private static String STATISTIC = "СТАТИСТИКА_ТОВАРОВ";
    private static ServerAddress seed = new ServerAddress("localhost", 27017);
    private static MongoDatabase database;
    private static MongoCollection<Document> collectionMag;
    private static MongoCollection<Document> collectionGoods;

    public static void main(String[] args) throws IOException {

        System.out.println("Введите команды: " + "\n" +
                "ДОБАВИТЬ_МАГАЗИН название_магазина" + "\n" +
                "ДОБАВИТЬ_ТОВАР наименование_товара цена_товара_целое_число" + "\n" +
                "ВЫСТАВИТЬ_ТОВАР наименование_товара название_магазина" + "\n" + "СТАТИСТИКА_ТОВАРОВ");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(seed)))
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("local");
        collectionMag = database.getCollection("magazine");
        collectionMag.drop();
        collectionGoods = database.getCollection("goods");
        collectionGoods.drop();

        while (true) {

            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

            String[] commands = getAction(command);
            if (commands[0].length() == 16)  {//"ДОБАВИТЬ_МАГАЗИН") {
                addMagazine(commands[1]);
            } else if (commands[0].length() == 14) { // "ДОБАВИТЬ_ТОВАР") {
                addGoods(commands[1], Integer.parseInt(commands[2]));
            } else if (commands[0].length() == 15)  { // "ВЫСТАВИТЬ_ТОВАР") {
                putGoods(commands[1], commands[2]);
            } else if (commands[0].length() == 18) {// "СТАТИСТИКА_ТОВАРОВ") {
                statistic();
            } else {
                System.out.println("Неверная команда!");
                System.out.println("Введите команды: " + "\n" +
                        "ДОБАВИТЬ_МАГАЗИН название_магазина" + "\n" +
                        "ДОБАВИТЬ_ТОВАР наименование_товара цена_товара_целое_число" + "\n" +
                        "ВЫСТАВИТЬ_ТОВАР наименование_товара название_магазина" + "\n" + "СТАТИСТИКА_ТОВАРОВ");
            }

        }

    }

    public static String[] getAction(String command) {
        String[] words = command.trim().split(" ");
           return words;
    }

    // add magazine
    public static void addMagazine(String nameOfMagazine) {
        if (collectionMag.find(new Document("name", nameOfMagazine)).first() == null) {
            Document document = new Document("name", nameOfMagazine);
            document.append("goods", new ArrayList<String>());
            collectionMag.insertOne(document);
            System.out.println("Магазин " + nameOfMagazine + " добавлен");
        } else {
            System.out.println("Такой магазин уже есть");
        }

    }

    // add goods
    public static void addGoods(String nameOfGoods, int price) {
        if (collectionGoods.find(new Document("name", nameOfGoods)).first() == null) {
            Document document = new Document("name", nameOfGoods);
            document.append("price", price);
            collectionGoods.insertOne(document);
            System.out.println("Товар " + nameOfGoods + " добавлен");
        } else {
            System.out.println("Такой товар уже есть");
        }
        collectionGoods.find().forEach((Consumer<Document>) document -> {
            System.out.println(document + "\n");
        });

    }

    // set goods
    public static void putGoods(String nameOfGoods, String nameOfMagazine) {
        Document magazine = collectionMag.find(new Document("name", nameOfMagazine)).first();
        Document goods = collectionGoods.find(new Document("name", nameOfGoods)).first();

        if (goods != null && magazine != null) {
            collectionMag.updateOne(magazine, new Document("$addToSet", new Document("goods", nameOfGoods)));
            System.out.println("Товар " + nameOfGoods + " добавлен в магазин " + nameOfMagazine);
        }  else  {
        System.out.println("Магазин или товар не найден");
        }

        collectionMag.find().forEach((Consumer<Document>) document -> {
            System.out.println(document + "\n");
        });

    }

    // statistic
    public static void statistic() {
        AggregateIterable<Document> documents = collectionGoods.aggregate(
                Arrays.asList(
                        Aggregates.lookup("magazine", "name", "goods", "magazine_list"),
                        Aggregates.unwind("$magazine_list"),
                        Aggregates.group("$magazine_list.name",
                                Accumulators.sum("count_goods", 1),
                                Accumulators.min("min_price", "$price"),
                                Accumulators.max("max_price", "$price"),
                                Accumulators.avg("avg_price", "$price"))
                ));

        for (Document document : documents) {
            String nameOfMagazine = (String) document.get("_id");
            System.out.println("Магазин " + nameOfMagazine);
            System.out.println("Количество товаров: " + document.get("count_goods"));
            System.out.println("Средняя цена товаров: " + document.get("avg_price"));
            System.out.println("Самый дорогой товар:  " + document.get("max_price"));
            System.out.println("Самый дешевый товар:  " + document.get("min_price"));
            System.out
                    .println("Количество товаров дешевле 100 рублей: " + cheapestGoodsNumber(nameOfMagazine));
            System.out.println();
        }
    }


    private static long cheapestGoodsNumber(String nameOfMagazine) {
        long number = 0;
        Document magazine = collectionMag.find(new Document("name", nameOfMagazine)).first();
        ArrayList<String> goods = (ArrayList<String>) magazine.get("goods");
        number = goods.stream()
           .filter(n -> (int) collectionGoods.find(new Document("name", n)).first().get("price") < 100).count();

        return number;
    }

}
