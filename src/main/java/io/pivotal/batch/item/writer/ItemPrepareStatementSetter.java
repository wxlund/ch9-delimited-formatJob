package io.pivotal.batch.item.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ItemPrepareStatementSetter<T> {
    void setValues(T item, PreparedStatement ps) throws SQLException;
}
