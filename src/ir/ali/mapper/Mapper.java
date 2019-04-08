package ir.ali.mapper;

import ir.ali.mapper.annotations.MapTo;
import ir.ali.mapper.annotations.NotMap;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 7e3t
 * this mapper maps two objects into each other
 */

public class Mapper {

    /**
     * depends on your package name, mine is <bold>ir.ali.mapper</bold>
     */
    private static final String PACKAGE = "ir.ali.mapper";

    /**
     * get string
     */
    private static final String GET = "get";

    /**
     * set string
     */
    private static final String SET = "set";

    /**
     * List string
     */
    private static final String LIST = "List";

    /**
     * package of java.util.List
     */
    private static final String LIST_PACKAGE = "java.util";

    /**
     * full name of java.util.List
     */
    private static final String LIST_FULL_NAME = "java.util.List";

    /**
     * this mapper will map without first initialization of entity
     * @param entityType type of entity
     * @param dto dto
     * @param <T> entity class
     * @param <U> dto class
     * @return the initialized entity
     * @throws Exception every thing that happened
     */
    public static <T,U> T map(Class<T> entityType, U dto) throws Exception {
        Objects.requireNonNull(entityType);
        T entity = entityType.getDeclaredConstructor().newInstance();
        map(entity,dto);
        return entity;
    }

    /**
     * this method will map two object into each other
     * @param entity entity
     * @param dto dto
     * @param <T> entity class
     * @param <U> dto class
     * @throws Exception every thing happened
     */
    public static <T,U> void map(T entity, U dto) throws Exception {

        List<Field> entityFields                = Arrays.asList(entity.getClass().getDeclaredFields());
        Map<Field,String> goingToBeMapEntityFields = entityFields .stream()
                .filter(field -> Objects.isNull(field.getAnnotation(NotMap.class)))
                .collect(Collectors.toMap(field -> field,field -> Objects.isNull(field.getAnnotation(MapTo.class))
                        ? field.getName()
                        : field.getAnnotation(MapTo.class).value()));

        for(Map.Entry<Field,String> pair : goingToBeMapEntityFields.entrySet()) {

            Field field = pair.getKey();
            String destination = pair.getValue();

            if (isList(field.getType())) {
                Class entityListType = getListType(field);
                if(entityListType.getPackageName().contains(PACKAGE) && !field.getType().isEnum()) {
                    if(Objects.isNull(entity.getClass().getDeclaredMethod(getGetFieldName(field)).invoke(entity))) {
                        entity.getClass().getDeclaredMethod(getSetFieldName(field), List.class).invoke(entity, new ArrayList<>());
                        List entityNeedMapList = (List) entity.getClass().getDeclaredMethod(getGetFieldName(field)).invoke(entity);
                        List dtoNeedMapList = (List) dto.getClass().getMethod(getGetFieldName(destination)).invoke(dto);
                        for (Object dtoNeedMapObject : dtoNeedMapList) {
                            Object entityNeedMapObject = map(entityListType, dtoNeedMapObject);
                            entityNeedMapList.add(entityNeedMapObject);
                        }
                    } else {
                        List entityNeedMapList = (List) entity.getClass().getDeclaredMethod(getGetFieldName(field)).invoke(entity);
                        List dtoNeedMapList = (List) dto.getClass().getMethod(getGetFieldName(destination)).invoke(dto);
                        for (int i = 0; i < entityNeedMapList.size(); i++) {
                            map(entityNeedMapList.get(i),dtoNeedMapList.get(i));
                        }
                    }
                } else {
                    entity.getClass().getDeclaredMethod(getSetFieldName(field),field.getType()).invoke(entity,dto.getClass().getMethod(getGetFieldName(destination)).invoke(dto));
                }
            } else {
                if (field.getType().getPackageName().contains(PACKAGE) && !field.getType().isEnum()) {
                    Object entityObject = map(field.getType(), dto.getClass().getMethod(getGetFieldName(field)).invoke(dto));
                    entity.getClass().getDeclaredMethod(getSetFieldName(field),field.getType()).invoke(entity, entityObject);

                } else {
                    entity.getClass().getDeclaredMethod(getSetFieldName(field),field.getType()).invoke(entity, dto.getClass().getDeclaredMethod(getGetFieldName(destination)).invoke(dto));
                }
            }
        }
    }

    /**
     * this method will find out whether or not one type is list
     * @param type type for process
     * @return result of process
     */
    private static boolean isList(Class type) {
        return type.getPackageName().contains(LIST_PACKAGE) && type.getSimpleName().contains(LIST);
    }

    /**
     * this method will generate a string depends on field for getter method
     * @param field field
     * @return result
     */
    private static String getGetFieldName(Field field) {
        return GET + String.valueOf(field.getName().charAt(0)).toUpperCase() + field.getName().substring(1);
    }

    /**
     * this method will generate a string depends on name of an attribute for getter method
     * @param name name of attribute
     * @return result
     */
    private static String getGetFieldName(String name) {
        return GET + String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
    }

    /**
     * this method will generate a string depends on field for setter method
     * @param field field
     * @return result
     */
    private static String getSetFieldName(Field field) {
        return SET + String.valueOf(field.getName().charAt(0)).toUpperCase() + field.getName().substring(1);
    }

    /**
     * this method will find out that what is the generic type of a field with type of List
     * @param field the filed with type of list
     * @return the type
     * @throws ClassNotFoundException
     */
    private static Class getListType(Field field) throws ClassNotFoundException {
        StringBuilder stringBuilder = new StringBuilder().append(field.getGenericType().getTypeName()).delete(0,LIST_FULL_NAME.length()).deleteCharAt(0);
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        String entityListTypeFullString = stringBuilder.toString();
        return  Class.forName(entityListTypeFullString);
    }

}
