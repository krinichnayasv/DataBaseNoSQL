package main;

import com.mongodb.ClientSessionOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.opencsv.CSVReader;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class Students {

    private static final String STUDENTS_CSV = "data/mongo.csv";
    private static List<ParseStudentsCSV> students;

    public static List<ParseStudentsCSV> parseLines (String pathStudentsCsv) {

        students = new ArrayList<>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(pathStudentsCsv));
            String[] fragments = reader.readNext();
            if (fragments.length != 3) {
                System.out.println("Wrong line: " + fragments);
            }
            while ((fragments = reader.readNext()) != null) {
                students.add(new ParseStudentsCSV(
                        fragments[0],
                        Integer.parseInt(fragments[1]),
                        parseCourses(fragments[2])
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

     //   System.out.println(students.size());

        return  students;
    }

    private static ArrayList<String> parseCourses (String text) {

        ArrayList<String> courses = new ArrayList<>();

        String[] words = text.trim().split(",");
        for (int i = 0; i < words.length; i++) {
            courses.add(words[i].trim());
        }
        return courses;
    }


    public static void main(String[] args) {

     parseLines(STUDENTS_CSV);

     //   ConnectionString connectionString = new ConnectionString("mongodb://host:27017/");
      //  MongoClient mgClient = MongoClients.create(connectionString);


        ServerAddress seed = new ServerAddress("localhost", 27017);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(seed)))
                .build();
        MongoClient mongoClient = MongoClients.create(settings);


        MongoDatabase database = mongoClient.getDatabase("local");

        // Создаем коллекцию
        MongoCollection<Document> collection = database.getCollection("students");

        // Удалим из нее все документы
        collection.drop();

        // создание объекта
        for(ParseStudentsCSV data : students) {

        Document document = new Document();
        document.append("name", data.getName());
        document.append("age", data.getAge());
        document.append("courses", data.getCourses());

        // вставка объекта в коллекцию
        database.getCollection("students").insertOne(document);
        }

        // вывод всей инфомации
//        database.getCollection("students").find().forEach((Consumer<Document>) document -> {
//            System.out.println(document + "\n");
//        });

        System.out.println("Кол-во студентов в базе: " + database.getCollection("students").countDocuments());

        // сортировка

        BsonDocument query1 = BsonDocument.parse("{age: {$gt: 40}}");

//        database.getCollection("students").find(query).forEach((Consumer<Document>) document -> {
//            System.out.println("Старше 40: " + document + "\n");
//        });

        System.out.println("Кол-во студентов старше 40 лет: "
                +   database.getCollection("students").countDocuments(query1));

        BsonDocument query2 = BsonDocument.parse("{age: 1}");

        database.getCollection("students").find().sort(query2).limit(1).forEach((Consumer<Document>) document ->
                System.out.println("Самый молодой студент: " + document.get("name") ));

        BsonDocument query3 = BsonDocument.parse("{age: -1}");

        database.getCollection("students").find().sort(query3).limit(1).forEach((Consumer<Document>) document ->
                System.out.println("Курсы самыого старшего студента: " + document.get("courses") ));

    }
}
