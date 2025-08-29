package ir.mrstudios.databasecloner.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;

@UtilityClass
public class NumberUtil {

    @SuppressWarnings("unchecked")
    public <T, N extends Number> T cast(N anyNumber, Class<T> toType) {
        if (toType == null) {
            throw new IllegalArgumentException("toType cannot be null");
        }
        if (anyNumber == null) {
            return null;
        }

        if (toType == Byte.class || toType == byte.class) {
            return (T) Byte.valueOf(anyNumber.byteValue());
        } else if (toType == Short.class || toType == short.class) {
            return (T) Short.valueOf(anyNumber.shortValue());
        } else if (toType == Integer.class || toType == int.class) {
            return (T) Integer.valueOf(anyNumber.intValue());
        } else if (toType == Float.class || toType == float.class) {
            return (T) Float.valueOf(anyNumber.floatValue());
        } else if (toType == Double.class || toType == double.class) {
            return (T) Double.valueOf(anyNumber.doubleValue());
        } else if (toType == Long.class || toType == long.class) {
            return (T) Long.valueOf(anyNumber.longValue());
        } else if (toType == BigInteger.class) {
            return (T) new BigInteger(anyNumber.toString());
        } else if (toType == BigDecimal.class) {
            return (T) new BigDecimal(anyNumber.toString());
        } else {
            throw new IllegalArgumentException("Cannot cast " + anyNumber + " to " + toType);
        }
    }

}