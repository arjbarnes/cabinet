package com.alexbarnes.cabinet;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;


public class App
{
    public static void main( String[] args )
    {
        runEmbeddedJettyServer(8080);
    }

    private static void runEmbeddedJettyServer(int port_)
    {
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

      context.setBaseResource(Resource.newResource(App.class.getResource("/web-app")));
      context.addServlet(DefaultServlet.class, "/");
      context.setWelcomeFiles(new String[]{"index.html"});

      ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/api/*");
      jerseyServlet.setInitOrder(0);
      jerseyServlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "com.alexbarnes.cabinet");
      jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, "org.glassfish.jersey.media.multipart.MultiPartFeature");

      Server jettyServer = new Server();
      jettyServer.setHandler(context);

      HttpConfiguration httpConfiguration = new HttpConfiguration();
      httpConfiguration.setSecureScheme("https");
      httpConfiguration.setSecurePort(443);
      ServerConnector httpConnector = new ServerConnector(jettyServer, new HttpConnectionFactory(httpConfiguration));
      httpConnector.setPort(80);
      jettyServer.addConnector(httpConnector);

      HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
      httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
      String keyStoreFilePath = "./cabinet.jks";
      String keyStorePassword = "Cabinet";
      File keyStoreFile = new File(keyStoreFilePath);
      if(!keyStoreFile.exists())
      {
        createNewKeyStoreWithKeyPair(keyStoreFilePath, keyStorePassword);
      }
      // ToDo: Check expiry date of certificate and generate new one if necessary
      SslContextFactory sslContextFactory = new SslContextFactory(keyStoreFilePath);
      sslContextFactory.setKeyStorePassword("Cabinet");
      final ServerConnector httpsConnector = new ServerConnector(jettyServer,
                                                                 new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                                                                 new HttpConnectionFactory(httpsConfiguration));
      httpsConnector.setPort(443);
      jettyServer.addConnector(httpsConnector);

      try
      {
        jettyServer.start();
        jettyServer.join();
      }
      catch(Exception e_)
      {
        System.out.println(e_.toString());
        // ToDo
      }
      finally
      {
        jettyServer.destroy();
      }
    }

    private static void createNewKeyStoreWithKeyPair(String keyStoreFilePath_, String keyStorePassword_)
    {
      try
      {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = keyStorePassword_.toCharArray();
        keyStore.load(null, password);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        System.out.println("Creating certificate");

        Security.addProvider(new BouncyCastleProvider());

        Calendar cal = Calendar.getInstance();
        Date certificateStartDate = cal.getTime();
        cal.add(Calendar.YEAR, 1000);
        Date certificateExpiryDate = cal.getTime();
        BigInteger certificateSerialNo = new BigInteger(Long.toString(certificateStartDate.getTime()));
        X509V1CertificateGenerator certificateGenerator = new X509V1CertificateGenerator();
        X500Principal dnName = new X500Principal("CN=Cabinet Web Service Certificate");
        certificateGenerator.setSerialNumber(certificateSerialNo);
        certificateGenerator.setIssuerDN(dnName);
        certificateGenerator.setNotBefore(certificateStartDate);
        certificateGenerator.setNotAfter(certificateExpiryDate);
        certificateGenerator.setSubjectDN(dnName);
        certificateGenerator.setPublicKey(publicKey);
        certificateGenerator.setSignatureAlgorithm("SHA256withRSA");
        X509Certificate certificate = certificateGenerator.generate(privateKey, "BC");

        keyStore.setKeyEntry("Cabinet", privateKey, password, new Certificate[]{certificate});

        System.out.println("Certificate created");
        System.out.println(certificate.toString());

        FileOutputStream fos = new FileOutputStream(keyStoreFilePath_);
        keyStore.store(fos, password);
        fos.close();
      }
      catch(Exception e_)
      {
        //ToDo
        System.out.println(e_.getMessage());
      }
    }

    private static EntityManager _entityManager;

    public static EntityManager getEntityManager()
    {
      if(_entityManager == null)
      {
        _entityManager = Persistence.createEntityManagerFactory("cabinet").createEntityManager();
      }
      return _entityManager;
    }

    private static String fileStorageLocation = "./files";

    public static String getFileStorageLocation()
    {
      return fileStorageLocation;
    }
}
