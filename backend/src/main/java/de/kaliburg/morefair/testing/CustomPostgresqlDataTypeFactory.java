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

  // TODO: (Low Prio) Bad Base64 input character at 8: 45(decimal) is getting written as Error
  //  Message if i use Timestamp for Timestamp with time zone

  @Override
  public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
    DataType dataType;

    if (sqlTypeName.equals("BOOLEAN")) {
      dataType = DataType.BOOLEAN;
    } else if (sqlTypeName.equals("TIMESTAMP WITH TIME ZONE")) {
      dataType = DataType.TIMESTAMP;
    } else {
      dataType = super.createDataType(sqlType, sqlTypeName);
    }

    return dataType;
  }
}
