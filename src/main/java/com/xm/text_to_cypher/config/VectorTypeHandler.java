package com.xm.text_to_cypher.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VectorTypeHandler extends BaseTypeHandler<float[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    float[] parameter, JdbcType jdbcType) throws SQLException {
        // 将float数组转换为PostgreSQL vector格式字符串
        PGobject vector = new PGobject();
        vector.setType("vector");
        vector.setValue(arrayToVectorString(parameter));
        ps.setObject(i,vector); ;
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return vectorStringToArray(rs.getString(columnName));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return vectorStringToArray(rs.getString(columnIndex));
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return vectorStringToArray(cs.getString(columnIndex));
    }

    private String arrayToVectorString(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private float[] vectorStringToArray(String vector) {
        if (vector == null || vector.isEmpty()) return null;
        String cleaned = vector.replace("[", "").replace("]", "");
        String[] parts = cleaned.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}
