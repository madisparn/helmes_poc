package org.bashhead.helmes.dao;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.bashhead.helmes.model.UserData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserDataDao {

  private final JdbcTemplate tmpl;

  public UserDataDao(DataSource ds) {
    this.tmpl = new JdbcTemplate(ds);
  }

  public Optional<UserData> tryFind(UUID id) {
    String sql = """
        SELECT *,
          (SELECT ARRAY_AGG(sector_id) FROM user_data_sector fds WHERE fds.form_data_id = fd.id) sectors
          FROM user_data fd
         WHERE id = ?""";
    return tmpl.queryForStream(sql, (rs, idx) -> new UserData(
        rs.getObject("id", UUID.class),
        rs.getString("name"),
        rs.getBoolean("agreed_tos"),
        toLongList(rs.getArray("sectors"))
    ), id).findFirst();
  }

  private List<Long> toLongList(Array array) throws SQLException {
    return Arrays.stream((Object[]) array.getArray())
        .map(Long.class::cast)
        .sorted()
        .toList();
  }

  public void save(UUID id, String name, Boolean agreedTos, List<Long> sectors) {
    tmpl.update("""
            MERGE INTO user_data AS target
            USING (SELECT cast(? as uuid), cast(? as varchar(max)), cast(? as boolean)) AS source (id, name, agreed_tos)
            ON target.id = source.id
            WHEN NOT MATCHED THEN
              INSERT (id, name, agreed_tos)
              VALUES (source.id, source.name, source.agreed_tos)
            WHEN MATCHED THEN
              UPDATE SET name = source.name, agreed_tos = source.agreed_tos""",
        ps -> {
          var idx = 0;
          ps.setObject(++idx, id);
          ps.setString(++idx, name);
          ps.setBoolean(++idx, agreedTos);
        }
    );

    tmpl.update("DELETE FROM user_data_sector WHERE form_data_id = ?", id);
    tmpl.batchUpdate("INSERT INTO user_data_sector (form_data_id, sector_id) VALUES (?, ?)",
        sectors.stream().map(item -> new Object[] {id, item}).toList());
  }
}
