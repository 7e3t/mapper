package ir.ali.mapper.entity;

import ir.ali.mapper.annotations.PrimaryKey;

public class Phone {

    @PrimaryKey
    private String number;

    private PhoneType type;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public PhoneType getType() {
        return type;
    }

    public void setType(PhoneType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "number='" + number + '\'' +
                ", type=" + type +
                '}';
    }
}
