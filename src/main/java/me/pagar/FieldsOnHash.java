package me.pagar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.pagar.object.CanBecomeKeyValueVariable;
import me.pagar.object.CanLoadFieldsFromSources;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FieldsOnHash implements CanLoadFieldsFromSources, CanBecomeKeyValueVariable {

    //String => (number | string | Map)
    private Map<String, Object> fields = new HashMap<>();

    public FieldsOnHash(String jsonString){
        loadParametersFrom(jsonString);
    }

    public FieldsOnHash(Map<String, Object> parameters){
        loadParametersFrom(parameters);
    }

    public Map<String, Object> fields(){
        return this.fields;
    }

    public Integer getParameterAsInteger(String parameterName, Integer defaultValue) {
        Object parameter = getParameterReferenceOrDefault(parameterName, defaultValue);
        try {
            Integer parameterAsInteger = Integer.valueOf(String.valueOf(parameter));
            return parameterAsInteger;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getParameterAsString(String parameterName, String defaultValue) {
        Object parameter = getParameterReferenceOrDefault(parameterName, defaultValue);
        if(parameter == null){
            return "null";
        } else {
            String parameterAsString = String.valueOf(parameter);
            return parameterAsString;
        }
    }

    public Boolean getParameterAsBoolean(String parameterName, Boolean defaultValue) {
        Object parameter = getParameterReferenceOrDefault(parameterName, defaultValue);
        if (parameter != null && parameter instanceof Boolean) {
            Boolean parameterAsBoolean = Boolean.valueOf(String.valueOf(parameter));
            return parameterAsBoolean;
        } else {
            return defaultValue;
        }
    }

    public <T extends FieldsOnHash> List<T> getParameterAsList(String parameterName, List<T> defaultValue) {
        Object parameter = getParameterReferenceOrDefault(parameterName, defaultValue);
        return (List<T>) parameter;
    }

    public List<String> getParameterAsStringList(String parameterName, List<String> defaultValue) {
        Object parameter = getParameterReferenceOrDefault(parameterName, defaultValue);
        try {
            return (List<String>) parameter;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public <T extends FieldsOnHash> List<T> getParameterAsObjectList(String parameterName, Class<T> objectClass, List<T> defaultValue) {
        List<Map<String, Object>> parameterValue = (List<Map<String, Object>>) getParameterReferenceOrDefault(parameterName, defaultValue);
        try{
            List<T> instanciatedObjects = new ArrayList<>();
            for(Map<String, Object> map : parameterValue) {
                T objectInstance = objectClass.newInstance();
                objectInstance.loadParametersFrom(map);
                instanciatedObjects.add(objectInstance);
            }
            return instanciatedObjects;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public <T extends CanLoadFieldsFromSources> T getParameterCasted(String parameterName, T classInstance, T defaultValue) {
        Map<String, Object> clasInstanceParameters = getParameterAsMap(parameterName, null);
        if(clasInstanceParameters == null) {
            return defaultValue;
        } else {
            classInstance.loadParametersFrom(clasInstanceParameters);
            return classInstance;
        }
    }

    public Map<String, Object> getParameterAsMap(String parameterName, Map<String, Object> defaultValue) {
        Object parameterValue = this.getParameterReferenceOrDefault(parameterName, defaultValue);
        try {
            return (Map<String, Object>)parameterValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void setParameter(String parameterName, String parameterValue){
        this.fields.put(parameterName, parameterValue);
    }

    public void setParameter(String parameterName, Integer parameterValue){
        this.fields.put(parameterName, parameterValue);
    }

    public void setParameter(String parameterName, Map<String, Object> parameterValue){
        this.fields.put(parameterName, parameterValue);
    }

    public void setParameter(String parameterName, Boolean parameterValue){
        this.fields.put(parameterName, parameterValue);
    }

    public void setParameter(String parameterName, FieldsOnHash child){
        this.fields.put(parameterName, child.fields());
    }

    public <T extends FieldsOnHash> void setParameter(String parameterName, Collection<T> list) {
        List<Map<String, Object>> mappedObjects = list.stream()
                .map((item) -> item.fields())
                .collect(Collectors.toList());
        this.fields.put(parameterName, mappedObjects);
    }

    public void setParameterCollection(String parameterName, Collection<String> collection) {
        this.fields.put(parameterName, collection);
    }

    @Override
    public void loadParametersFrom(Map<String, Object> parameters) {
        this.fields = parameters;
    }

    //TODO - parametrize Gson
    @Override
    public void loadParametersFrom(String jsonString) {
        Map<String, Object> hashedJson = new Gson().fromJson(jsonString, HashMap.class);
        loadParametersFrom(hashedJson);
    }

    //TODO - parametrize Gson
    @Override
    public String toJson(){
        Gson gson = new GsonBuilder().serializeNulls().create();
        String jsonString = gson.toJson(this.fields);
        return jsonString;
    }

    public String toQueryString(){
        StringBuilder builder = new StringBuilder();
        toQueryParamsRecursive(new ArrayList<String>(), this.fields, builder);
        return builder.toString();
    }

    //TODO - make it readable...
    @SuppressWarnings("unchecked")
    private void toQueryParamsRecursive(List<String> context, Map<String, Object> fields, StringBuilder result){
        for (Map.Entry<String, Object> keyValuePair : fields.entrySet()) {
            String key = keyValuePair.getKey();
            Object value = keyValuePair.getValue();

            if(value instanceof Map) {
                List<String> newContext = new ArrayList<String>();
                newContext.addAll(context);
                newContext.add(key);
                toQueryParamsRecursive(newContext, (Map<String, Object>)value, result);
            } else if(value instanceof String || value instanceof Integer || value instanceof Boolean) {
                if(context.size() > 0){
                    String prefix = context.get(0);
                    for(int i = 1; i < context.size(); i++){
                        prefix += "[" + context.get(i) + "]";
                    }
                    result.append(prefix + "[" + key + "]=" + value + "&");
                }else{
                    result.append(key + "=" + value + "&");
                }
            } else if (value instanceof List) {
                List castedValue = (List)value;
                for (int i = 0; i < castedValue.size(); i++) {
                    if(context.size() > 0){
                        String prefix = context.get(0);
                        for(int j = 1; j < context.size(); j++){
                            prefix += "[" + context.get(j) + "]";
                        }
                        result.append(prefix + "[" + key + "][" + i + "]=" + castedValue.get(i) + "&");
                    }else{
                        result.append(key + "[" + i + "]=" + castedValue.get(i) + "&");
                    }
                }
            }
        }
    }

    protected Object getParameterReferenceOrDefault(String parameterName, Object defaultValue) {
        if(this.fields.containsKey(parameterName)) {
            Object parameterValue = this.fields.get(parameterName);
            return parameterValue;
        } else {
            return defaultValue;
        }
    }

    public boolean equals(FieldsOnHash other) {
        return this.fields.equals(other.fields);
    }
}
