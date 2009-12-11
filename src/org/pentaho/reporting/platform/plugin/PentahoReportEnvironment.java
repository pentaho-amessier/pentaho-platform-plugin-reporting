package org.pentaho.reporting.platform.plugin;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.reporting.engine.classic.core.DefaultReportEnvironment;
import org.pentaho.reporting.engine.classic.core.modules.output.csv.CSVQuoter;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.platform.plugin.messages.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PentahoReportEnvironment extends DefaultReportEnvironment
{
  private static final Log logger = LogFactory.getLog(PentahoReportEnvironment.class);

  public PentahoReportEnvironment(final Configuration configuration)
  {
    super(configuration);
  }

  public String getEnvironmentProperty(final String key)
  {
    if (key == null)
    {
      throw new NullPointerException();
    }

    final IPentahoSession session = PentahoSessionHolder.getSession();
    if (PentahoSystem.getApplicationContext() != null)
    {
      final String pentahoBaseURL = PentahoSystem.getApplicationContext().getBaseUrl();
      if ("serverBaseURL".equals(key)) //$NON-NLS-1$
      {
        return getBaseServerURL(pentahoBaseURL);
      }
      else if ("pentahoBaseURL".equals(key)) //$NON-NLS-1$
      {
        return pentahoBaseURL;
      }
      else if ("solutionRoot".equals(key)) //$NON-NLS-1$
      {
        return PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$
      }
      else if ("hostColonPort".equals(key)) //$NON-NLS-1$
      {
        return getHostColonPort(pentahoBaseURL);
      }
    }
    else
    {
      if ("serverBaseURL".equals(key) || //$NON-NLS-1$
          "pentahoBaseURL".equals(key) || //$NON-NLS-1$
          "solutionRoot".equals(key) || //$NON-NLS-1$
          "hostColonPort".equals(key)) //$NON-NLS-1$
      {
        logger.warn(Messages.getString("ReportPlugin.warnNoApplicationContext"));
        // make it explicit that these values are not available. This way
        // a configuration in the classic-engine.properties file cannot begin
        // to interfer here.
        return null;
      }
    }
    if (session != null)
    {
      if ("username".equals(key)) //$NON-NLS-1$
      {
        return session.getName();
      }
      else if ("roles".equals(key)) //$NON-NLS-1$
      {
        final IUserDetailsRoleListService roleListService = PentahoSystem.get(IUserDetailsRoleListService.class);
        if (roleListService == null)
        {
          return null;
        }
        final StringBuffer property = new StringBuffer();
        //noinspection unchecked
        final List<String> roles =
            (List<String>) roleListService.getRolesForUser(session.getName());
        if (roles == null)
        {
          return null;
        }

        final int rolesSize = roles.size();
        if (rolesSize > 0)
        {
          final CSVQuoter quoter = new CSVQuoter(",");//$NON-NLS-1$
          property.append(roles.get(0));
          for (int i = 1; i < rolesSize; i++)
          {
            property.append(",");//$NON-NLS-1$
            property.append(quoter.doQuoting(roles.get(i)));
          }
        }
        return property.toString();
      }

      if (key.startsWith("session:"))//$NON-NLS-1$
      {
        final Object attribute = session.getAttribute(key.substring("session:".length()));//$NON-NLS-1$
        if (attribute instanceof String)
        {
          return (String) attribute;
        }
        return null;
      }
    }
    else
    {
      if (key.startsWith("session:") ||
          key.equals("username") ||
          key.equals("roles"))
      {
        logger.warn(Messages.getString("ReportPlugin.warnNoSession"));
        return null;
      }
    }

    return super.getEnvironmentProperty(key);
  }

  private String getBaseServerURL(final String pentahoBaseURL)
  {
    try
    {
      final URL url = new URL(pentahoBaseURL);
      return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch (Exception e)
    {
      // ignore
    }
    return pentahoBaseURL;
  }

  private String getHostColonPort(final String pentahoBaseURL)
  {
    try
    {
      final URL url = new URL(pentahoBaseURL);
      return url.getHost() + ":" + url.getPort();//$NON-NLS-1$ 
    }
    catch (Exception e)
    {
      // ignore 
    }
    return pentahoBaseURL;
  }

  public Locale getLocale()
  {
    return LocaleHelper.getLocale();
  }

  public String getURLEncoding()
  {
    return LocaleHelper.getSystemEncoding();
  }
}
