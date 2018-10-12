package io.configrd.service;

import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import io.configrd.core.exception.AuthenticationException;
import io.configrd.core.exception.InitializationException;

public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {

  private static final Logger logger = LoggerFactory.getLogger("ConfigrdService");
  
  @Override
  public Response toResponse(Throwable arg0) {

    Throwable t = Throwables.getRootCause(arg0);
    logger.error(t.getMessage(), t);

    if (t instanceof InitializationException) {

    } else if (t instanceof AuthenticationException) {

    }

    return Response.serverError().entity(t.getMessage()).build();
  }

}
