package ir.ali.mapper;

import ir.ali.mapper.annotations.MapTo;
import ir.ali.mapper.annotations.NotMap;
import ir.ali.mapper.annotations.PrimaryKey;
import ir.ali.mapper.exceptions.*;

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
    public static <T,U> T map(Class<T> entityType, U dto, String... unMapFields) throws Exception {
        if (Objects.isNull(entityType)) {
            throw new NullArgumentException();
        }
        T entity = entityType.getDeclaredConstructor().newInstance();
        map(entity, dto, unMapFields);
        return entity;
    }

    /**
     * this method will map two object into each other
     * @param entity entity
     * @param dto dto
     * @param <T> entity class
     * @param <U> dto class
     * @throws MapperException every thing happened
     */
    public static <T,U> void map(T entity, U dto, String... unMapFields) throws MapperException {

        List<String> unMapFieldsList = Arrays.asList(unMapFields);

        if (Objects.isNull(entity) || Objects.isNull(dto)) {
            throw new NullArgumentException();
        }

        List<Field> entityFields = getAllFields(entity.getClass());

        Map<Field,String> goingToBeMapEntityFields = entityFields .stream()
                .filter(field -> Objects.isNull(field.getAnnotation(NotMap.class)) && !unMapFieldsList.contains(field.getName()))
                .collect(Collectors.toMap(field -> field,field -> Objects.isNull(field.getAnnotation(MapTo.class))
                        ? field.getName()
                        : field.getAnnotation(MapTo.class).value()));

        for(Map.Entry<Field,String> pair : goingToBeMapEntityFields.entrySet()) {

            Field field = pair.getKey();
            String destination = pair.getValue();

            List<String> specificUnMapFiledList = new ArrayList<>(unMapFieldsList.stream().filter(s -> s.contains(field.getName()+".")).collect(Collectors.toList()));
            String[] specificUnMapFiledArray = new String[specificUnMapFiledList.size()];
            for (int i = 0; i < specificUnMapFiledList.size(); i++) {
                specificUnMapFiledList.set(i, specificUnMapFiledList.get(i).substring(field.getName().length()+1));
                specificUnMapFiledArray[i] = specificUnMapFiledList.get(i);
            }

            try {
                if (isList(field.getType())) {
                    Class entityListType = getListType(field);
                    if(entityListType.getPackageName().contains(PACKAGE) && !field.getType().isEnum()) {
                        if(Objects.isNull(entity.getClass().getMethod(getGetFieldName(field)).invoke(entity))) {
                            entity.getClass().getMethod(getSetFieldName(field), List.class).invoke(entity, new ArrayList<>());
                            List entityNeedMapList = (List) entity.getClass().getMethod(getGetFieldName(field)).invoke(entity);
                            List dtoNeedMapList = (List) dto.getClass().getMethod(getGetFieldName(destination)).invoke(dto);
                            for (Object dtoNeedMapObject : dtoNeedMapList) {
                                Object entityNeedMapObject = map(entityListType, dtoNeedMapObject, specificUnMapFiledArray);
                                entityNeedMapList.add(entityNeedMapObject);
                            }
                        } else {
                            List entityNeedMapList = (List) entity.getClass().getMethod(getGetFieldName(field)).invoke(entity);
                            List dtoNeedMapList = (List) dto.getClass().getMethod(getGetFieldName(destination)).invoke(dto);

                            // find primary key field from entityNeedMapListObject
                            Field primaryKeyField = getPrimaryKey(entityListType);

                            if (Objects.isNull(primaryKeyField)) {
                                throw new UnsetPrimaryKey();
                            }

                            for (int i = 0; i < entityNeedMapList.size(); i++) {
                                // get primary key object depends on primary key field from entity
                                Object entityPrimaryKeyObject = entityNeedMapList.get(i).getClass().getMethod(getGetFieldName(primaryKeyField)).invoke(entityNeedMapList.get(i));
                                if (Objects.isNull(entityPrimaryKeyObject)) {
                                    throw new UnsetPrimaryKey();
                                }
                                for (int ii = 0; ii < dtoNeedMapList.size(); ii++) {
                                    // get primary key object depends on primary key field from dto
                                    Object dtoPrimaryKeyObject = dtoNeedMapList.get(ii).getClass().getMethod(getGetFieldName(primaryKeyField)).invoke(dtoNeedMapList.get(ii));
                                    if (Objects.isNull(dtoPrimaryKeyObject)) {
                                        throw new UnsetPrimaryKey();
                                    }
                                    // find equality of primary key object and entityNeedMapList
                                    if (entityPrimaryKeyObject.equals(dtoPrimaryKeyObject)) {
                                        map(entityNeedMapList.get(ii),dtoNeedMapList.get(i), specificUnMapFiledArray);
                                        break;
                                    }
                                    if (ii == dtoNeedMapList.size() - 1) {
                                        // delete if was not found
                                        entityNeedMapList.remove(i);
                                    }
                                }
                            }

                            for (int i = 0; i < dtoNeedMapList.size(); i++) {
                                // get primary key object depends on primary key field from dto
                                Object dtoPrimaryKeyObject = dtoNeedMapList.get(i).getClass().getMethod(getGetFieldName(primaryKeyField)).invoke(dtoNeedMapList.get(i));
                                if (Objects.isNull(dtoPrimaryKeyObject)) {
                                    throw new UnsetPrimaryKey();
                                }
                                for (int ii = 0; ii < entityNeedMapList.size(); ii++) {
                                    // get primary key object depends on primary key field from entity
                                    Object entityPrimaryKeyObject = entityNeedMapList.get(ii).getClass().getMethod(getGetFieldName(primaryKeyField)).invoke(entityNeedMapList.get(ii));
                                    if (Objects.isNull(entityPrimaryKeyObject)) {
                                        throw new UnsetPrimaryKey();
                                    }
                                    // find equality of primary key object and entityNeedMapList
                                    if (entityPrimaryKeyObject.equals(dtoPrimaryKeyObject)) {
                                        break;
                                    }
                                    if (ii == entityNeedMapList.size() - 1) {
                                        // insert new object into entityNeedMapList
                                        Object entityNeedMapObject = map(entityListType, dtoNeedMapList.get(i), specificUnMapFiledArray);
                                        entityNeedMapList.add(entityNeedMapObject);
                                    }
                                }
                            }
                        }
                    } else {
                        entity.getClass().getMethod(getSetFieldName(field),field.getType()).invoke(entity,dto.getClass().getMethod(getGetFieldName(destination)).invoke(dto));
                    }
                } else {
                    if (field.getType().getPackageName().contains(PACKAGE) && !field.getType().isEnum()) {
                        Object entityObject = map(field.getType(), dto.getClass().getMethod(getGetFieldName(field)).invoke(dto), specificUnMapFiledArray);
                        entity.getClass().getMethod(getSetFieldName(field),field.getType()).invoke(entity, entityObject);

                    } else {
                        entity.getClass().getMethod(getSetFieldName(field),field.getType()).invoke(entity, dto.getClass().getMethod(getGetFieldName(destination)).invoke(dto));
                    }
                }
            } catch (ClassCastException e) {
                throw new NotMatchFieldException();
            } catch (NoSuchMethodException e) {
                throw new NotMatchArgumentException();
            } catch (UnsetPrimaryKey t) {
                throw new UnsetPrimaryKey();
            } catch (Throwable t) {
                throw new UndefinedException();
            }
        }
    }

    private static Field getPrimaryKey(Class type) {
        List<Field> entityFields = getAllFields(type);
        for (Field field : entityFields) {
            if (Objects.nonNull(field.getDeclaredAnnotation(PrimaryKey.class))) {
                return field;
            }
        }
        return null;
    }

    private static List<Field> getAllFields(Class type) {
        List<Field> fields = new ArrayList<>();
        do {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        } while (!type.getName().equals(Object.class.getName()));
        return fields;
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
