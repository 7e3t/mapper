package ir.ali.mapper.dto;

import ir.ali.mapper.entity.PhoneType;

public class PhoneDto {

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
}
