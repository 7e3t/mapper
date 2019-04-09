package ir.ali.mapper;

import ir.ali.mapper.dto.PersonDto;
import ir.ali.mapper.dto.PhoneDto;
import ir.ali.mapper.entity.Person;
import ir.ali.mapper.entity.Phone;
import ir.ali.mapper.entity.PhoneType;

import java.util.ArrayList;
import java.util.List;

public class RunDemo {

    public static void main(String[] args) throws Exception {
        PhoneDto phoneDto1 = new PhoneDto();
        phoneDto1.setNumber("+98912");
        phoneDto1.setType(PhoneType.MOBILE);

        PhoneDto phoneDto2 = new PhoneDto();
        phoneDto2.setNumber("+9821");
        phoneDto2.setType(PhoneType.HOME);

        List<PhoneDto> phones = new ArrayList<>();
        phones.add(phoneDto1);
        phones.add(phoneDto2);

        PersonDto personDto = new PersonDto();
        personDto.setAge(20);
        personDto.setFullName("ali dahaghin");
        personDto.setId(1001L);
        personDto.setPhones(phones);

        Person person = Mapper.map(Person.class, personDto);

        System.out.println(person);

        person.setName("rez");

        person.getPhones().get(1).setType(PhoneType.MOBILE);
        person.getPhones().remove(0);

        Phone phone3 = new Phone();
        phone3.setNumber("+98930");
        phone3.setType(PhoneType.MOBILE);

        Phone phone4 = new Phone();
        phone4.setNumber("+98937");
        phone4.setType(PhoneType.MOBILE);

        person.getPhones().add(phone3);
        person.getPhones().add(phone4);

        System.out.println(person);

        Mapper.map(person, personDto);

        System.out.println(person);
    }
}
