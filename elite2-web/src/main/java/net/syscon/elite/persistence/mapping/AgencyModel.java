package net.syscon.elite.persistence.mapping;


import net.syscon.elite.web.api.model.Agency;

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.SqlResultSetMapping;

@Entity
@SqlResultSetMapping(name = "findTaxaPedidoMapping", entities =
@EntityResult(entityClass = AgencyModel.class))
public class AgencyModel extends Agency {







}
