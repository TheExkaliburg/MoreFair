package de.kaliburg.morefair.testing;

import java.util.Arrays;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

@Slf4j
public class CustomPostgresqlDataTypeFactory extends PostgresqlDataTypeFactory {

  private static final Collection<String> DATABASE_PRODUCTS = Arrays.asList("PostgreSQL", "h2");

  @Override
  public Collection<String> getValidDbProducts() {
    return DATABASE_PRODUCTS;
  }

  @Override
  public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
    DataType dataType = super.createDataType(sqlType, sqlTypeName);

    if (dataType == DataType.UNKNOWN) {
      if (sqlType == 2014 && sqlTypeName.equals("TIMESTAMP WITH TIME ZONE")) {
        dataType = DataType.TIMESTAMP;
      }
    }

    return dataType;
  }
}
