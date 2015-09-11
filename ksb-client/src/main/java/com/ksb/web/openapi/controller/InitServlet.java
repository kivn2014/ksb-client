package com.ksb.web.openapi.controller;

import com.ksb.web.openapi.util.SysContent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.io.IOUtils;

public class InitServlet extends HttpServlet
{
  private static final long serialVersionUID = 8244142241834520763L;

  public void init()
    throws ServletException
  {
    readMyId();
  }

  public void readMyId() {
    int myid = 0;
    InputStream is = getClass().getResourceAsStream("/myid");
    if (is != null) {
      try {
        myid = Integer.valueOf(IOUtils.toString(is)).intValue();
      }
      catch (IOException e) {
        System.exit(0);
      }
      catch (NumberFormatException e) {
        System.exit(0);
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
    else {
      System.exit(0);
    }

    if (!SysContent.setMyid(myid)) {
      System.out.println(String.format("myid的内容必须在%d-%d之间，启动失败", new Object[] { Integer.valueOf(1), Integer.valueOf(32) }));
      System.exit(0);
    }
  }
}
