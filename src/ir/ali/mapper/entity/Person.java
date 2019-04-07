package ir.ali.mapper.entity;

import ir.ali.mapper.annotations.MapTo;
import ir.ali.mapper.annotations.NotMap;
import ir.ali.mapper.annotations.PrimaryKey;

import java.util.List;

public class Person {

    @PrimaryKey
    private Long id;

    @NotMap
    private Long version;

    @MapTo("fullName")
    private String name;

    private Integer age;

    private List<Phone> phones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", phones=" + phones +
                '}';
    }
}
