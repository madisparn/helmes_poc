package org.bashhead.helmes.dao;

import java.util.List;
import javax.sql.DataSource;
import org.bashhead.helmes.model.Sector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SectorDao {

  private final JdbcTemplate tmpl;

  public SectorDao(DataSource ds) {
    this.tmpl = new JdbcTemplate(ds);
  }

  public List<Sector> getAll() {
    String sql = """
        WITH RECURSIVE data(id, name, level, order_nr) AS (
            SELECT id, name, 0, order_nr FROM sector WHERE parent_id IS NULL
              UNION ALL
            SELECT s.id, s.name, d.level + 1, d.order_nr + s.order_nr/power(100.0, d.level + 1)
            FROM data d 
            JOIN sector s ON s.parent_id = d.id
        )
        SELECT * FROM data ORDER BY order_nr""";
    return tmpl.query(sql, (rs, idx) -> new Sector(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getInt("level")
    ));
  }
}
