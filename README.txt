Reset Password Forge plugin

This plugin is to be used with any Hippo 10 based project.
To use this plugin add a plugin version to your main pom properties and add the following dependencies:

/pom.xml:
    <dependency>
      <groupId>org.onehippo.forge</groupId>
      <artifactId>resetpassword</artifactId>
      <version>${forge.resetpassword}</version>
    </dependency>
This adds the main dependency

/bootstrap/configuration/pom.xml
    <dependency>
      <groupId>org.onehippo.forge</groupId>
      <artifactId>resetpassword-bootstrap-configuration</artifactId>
      <version>${forge.resetpassword}</version>
    </dependency>
Registers e-mail module, extended login page and the resetpassword functionality.

/bootstrap/content/pom.xml
    <dependency>
      <groupId>org.onehippo.forge</groupId>
      <artifactId>resetpassword-bootstrap-content</artifactId>
      <version>${forge.resetpassword}</version>
    </dependency>
Adds a content document to the repository at /content/documents/resetpassword/password-reset
containing all labels and duration configuration.

/cms/pom.xml
    <dependency>
      <groupId>org.onehippo.forge</groupId>
      <artifactId>resetpassword-cms</artifactId>
      <version>${forge.resetpassword}</version>
    </dependency>
Add cms functionality.

Additionally you need to add the following filter and filter-mapping to the cms web.xml:
/cms/src/main/webapp/WEB-INF/web.xml
  <filter>
    <filter-name>ResetPassword</filter-name>
    <filter-class>org.apache.wicket.protocol.http.WicketFilter</filter-class>
    <init-param>
      <param-name>applicationClassName</param-name>
      <param-value>org.onehippo.forge.resetpassword.frontend.ResetPasswordMain</param-value>
    </init-param>
    <init-param>
      <param-name>config</param-name>
      <param-value>resetpassword</param-value>
    </init-param>
    <init-param>
      <param-name>wicket.configuration</param-name>
      <param-value>deployment</param-value>
    </init-param>
    <init-param>
      <param-name>repository-address</param-name>
      <param-value>vm://</param-value>
    </init-param>
  </filter>

    <filter-mapping>
      <filter-name>ResetPassword</filter-name>
      <url-pattern>/resetpassword/*</url-pattern>
    </filter-mapping>