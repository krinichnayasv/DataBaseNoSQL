package main;

import java.util.ArrayList;
import java.util.List;

public class ParseStudentsCSV {


    private String name;
    private Integer age;
    private ArrayList<String> courses;

    List<ParseStudentsCSV> data = new ArrayList<>();

    public ParseStudentsCSV (String name, Integer age, ArrayList<String> courses) {
        this.name = name;
        this.age = age;
        this.courses = courses;

    }

    public void addParseData(ParseStudentsCSV parse){
        data.add(parse);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


    public ArrayList<String> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<String> courses) {
        this.courses = courses;
    }



}
