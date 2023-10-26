package com.anzi.rpc.serializer;

/**
 * 通用的序列化反序列化接口
 *
 * @author anzi
 */
public interface CommonSerializer {

    Integer KRYO_SERIALIZER = 0;
    Integer JSON_SERIALIZER = 1;
    Integer HESSIAN_SERIALIZER = 2;
    Integer PROTOBUF_SERIALIZER = 3;

    Integer DEFAULT_SERIALIZER = KRYO_SERIALIZER;

    CommonSerializer kryoSerializer = new KryoSerializer();
    CommonSerializer jsonSerializer = new JsonSerializer();
    CommonSerializer hessianSerializer = new HessianSerializer();
    CommonSerializer protobufSerializer = new ProtobufSerializer();

    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 0:
                return kryoSerializer;
            case 1:
                return jsonSerializer;
            case 2:
                return hessianSerializer;
            case 3:
                return protobufSerializer;
            default:
                return null;
        }
    }

    static CommonSerializer getByType(String type) {
        switch (type) {
            case "Kryo":
                return kryoSerializer;
            case "Json":
                return jsonSerializer;
            case "Hessian":
                return hessianSerializer;
            case "Protobuf":
                return protobufSerializer;
            default:
                return null;
        }
    }

    byte[] serialize(Object obj);

    Object deserialize(byte[] bytes, Class<?> clazz);

    int getCode();

}
