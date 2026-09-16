package com.devonfw.tools.ide.service;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devonfw.tools.ide.cli.CliException;
import com.devonfw.tools.ide.commandlet.Commandlet;
import com.devonfw.tools.ide.context.IdeContext;
import com.devonfw.tools.ide.log.IdeLogLevel;

public class ServiceServeCommandlet extends Commandlet {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceServeCommandlet.class);

  public ServiceServeCommandlet(IdeContext context) {
    super(context);
    addKeyword(getName());
  }


  @Override
  public String getName() {
    return "service-serve";
  }

  @Override
  public boolean isProcessableOutput() {
    return true;
  }

  @Override
  protected void doRun() {
    try {
      Path portFile = this.context.getIdePath().resolve(IdeServiceServer.PORT_FILE_NAME);
      IdeServiceServer server = new IdeServiceServer(this.context);
      int port = server.start(portFile);
      IdeLogLevel.INFO.log(LOG, "IDEasy service is listening on port {} (port file {})", port, portFile);
      LOG.info("Press Ctrl+C to stop the service");
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOG.error("Failed to start IDEasy service: {}", e.getMessage());
      throw new CliException("Failed to start IDEasy service");
    }
  }
}
