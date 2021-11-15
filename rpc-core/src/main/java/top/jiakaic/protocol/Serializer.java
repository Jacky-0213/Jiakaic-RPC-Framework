package top.jiakaic.protocol;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * @author JK
 * @date 2021/11/8 -23:06
 * @Description
 **/
public interface Serializer {
    <T> T deserializer(Class<T> clazz, byte[] bytes);

    <T> byte[] serializer(T obj);

    enum Algorithm implements Serializer {
        Java {
            @Override
            public <T> T deserializer(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    return (T) ois.readObject();
                } catch (Exception e) {
                    throw new RuntimeException("反序列化失败", e);
                }
            }

            @Override
            public <T> byte[] serializer(T obj) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oss = new ObjectOutputStream(bos);
                    oss.writeObject(obj);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("序列化失败", e);
                }
            }
        },
        Json {
            @Override
            public <T> T deserializer(Class<T> clazz, byte[] bytes) {
                String json = new String(bytes, StandardCharsets.UTF_8);
                return new GsonBuilder().registerTypeAdapter(Class.class,new ClassCodec()).create().fromJson(json, clazz);
            }

            @Override
            public <T> byte[] serializer(T obj) {
                String json = new GsonBuilder().registerTypeAdapter(Class.class,new ClassCodec()).create().toJson(obj);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }
    }
    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override             //   String.class
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            // class -> json
            return new JsonPrimitive(src.getName());
        }
    }
}
