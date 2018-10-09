package com.appcrossings.config;

import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.appcrossings.config.exception.AuthenticationException;
import com.appcrossings.config.exception.InitializationException;
import com.google.common.base.Throwables;

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
