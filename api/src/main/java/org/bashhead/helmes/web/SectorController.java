package org.bashhead.helmes.web;

import java.util.List;
import org.bashhead.helmes.dao.SectorDao;
import org.bashhead.helmes.model.Sector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SectorController {

  private final SectorDao dao;

  public SectorController(SectorDao dao) {
    this.dao = dao;
  }

  @GetMapping("/api/sector")
  public List<Sector> list() {
    return dao.getAll();
  }
}
