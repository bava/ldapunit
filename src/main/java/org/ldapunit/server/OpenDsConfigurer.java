package org.ldapunit.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.opends.server.backends.MemoryBackend;
import org.opends.server.core.DirectoryServer;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryEnvironmentConfig;
import org.opends.server.util.EmbeddedUtils;


public class OpenDsConfigurer implements EmbeddedServerConfigurer 
{
	
	private static final String DEFAULT_SERVER_ROOT_DIRECTORY = "opends";
	
	
	private static final String JAR_CONFIG_DIRECTORY = "opends/config/";
	private static final String JAR_SCHEMA_DIRECTORY = "opends/config/schema/";
	private static final String JAR_UPGRADE_DIRECTOR = "opends/config/upgrade/";
	
	private String rootDn;
	private int port;
	private String serverRoot = DEFAULT_SERVER_ROOT_DIRECTORY;
		
	public OpenDsConfigurer(String rootDn, int port) 
	{
		this.rootDn = rootDn;
		this.port = port;
	}

	public void startServer() 
	{
		 try
         {
			 System.out.println("Starting Embedded OpenDS LDAP Server ....");
			 DirectoryEnvironmentConfig directoryConfig = getDirectoryConfig();
			 changeServerPort(directoryConfig.getConfigFile());
			 EmbeddedUtils.startServer(directoryConfig);
			 initializeTestBackend(true, rootDn);
			 System.out.println("Embedded OpenDS LDAP server has started successfully ....");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
	}
	
	public void stopServer() 
	{
		if (EmbeddedUtils.isRunning())
	    {
			System.out.println("Shutting down Embedded OpenDS LDAP Server ....");
			EmbeddedUtils.stopServer(this.getClass().getName(), null);
			System.out.println("Embedded OpenDS LDAP Server shutdown successful ....");
	    }
	}
	
	// Change the server port in the config file. This is currently the only way I was able to set the port 
	public void changeServerPort(File configFile)
	{
		try
		{
			String fileContent = FileUtils.readFileToString(configFile);
			// Replace the default port with the user specified port
			fileContent = fileContent.replaceFirst("ds-cfg-listen-port: 13389", "ds-cfg-listen-port: " + port);
			FileUtils.writeStringToFile(configFile, fileContent);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * For OpenDS to run, it would need the following directories:
	 * 	1. Root Directory with read/write privileges
	 *  2. Lock Directory
	 *  3. Schema Directory
	 *  
	 *  Read other stuff from initOpendsDirectory method
	 */
	protected DirectoryEnvironmentConfig getDirectoryConfig() 
	{
		DirectoryEnvironmentConfig config = new DirectoryEnvironmentConfig();		
		try 
		{
			File serverRoot = getServerRoot();
			config.setServerRoot(serverRoot);
			config.setForceDaemonThreads(true);
			
			// Create config directory and copy the required files
			File configDirectory = new File(serverRoot, "config");
			configDirectory.mkdir();

			String[] configFiles = {"aaaa.ldif", "admin-backend.ldif", "config.ldif"};
			copyFilesFromJar(configFiles, JAR_CONFIG_DIRECTORY, configDirectory);			

			File schemaDirectory = new File(configDirectory, "schema");
			schemaDirectory.mkdir();
			// Copy the schema files
			String[] schemaFiles = {"00-core.ldif", "01-pwpolicy.ldif", "02-config.ldif", "03-changelog.ldif", "03-rfc2713.ldif", 
									"03-rfc2714.ldif", "03-rfc2739.ldif", "03-rfc2926.ldif", "03-rfc3112.ldif", "03-rfc3712.ldif", 
									"03-uddiv3.ldif", "04-rfc2307bis.ldif", "05-rfc4876.ldif", "05-solaris.ldif", "06-compat.ldif"};
			copyFilesFromJar(schemaFiles, JAR_SCHEMA_DIRECTORY, schemaDirectory);			
						
			File upgradeDirectory = new File(configDirectory, "upgrade");
			upgradeDirectory.mkdir();
			// copy the upgrade files
			String[] upgradeFiles = {"config.ldif.6181", "schema.ldif.6181"};
			copyFilesFromJar(upgradeFiles, JAR_UPGRADE_DIRECTOR, upgradeDirectory);			
			
			// Create other required directories
			String[] subDirectories = { "changelogDb", "classes", "db", "ldif", "locks", "logs" };
		    for (String s : subDirectories) {
		    	 new File(serverRoot, s).mkdir();
		    }
		     
		    config.setConfigFile(new File(configDirectory, "config.ldif"));  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return config;
	}
		
	private void copyFilesFromJar(String[] jarFiles, String jarDirectory, File outputDirectory) throws IOException
	{
		for(String jarFile : jarFiles)
		{
			File outputFile = new File(outputDirectory, jarFile);
			InputStream in = getClass().getClassLoader().getResourceAsStream(jarDirectory + jarFile);
			FileUtils.copyInputStreamToFile(in, outputFile);
		}
	}
	
	
	public MemoryBackend initializeTestBackend(boolean createBaseEntry, String dn) throws Exception 
	{

        DN baseDN = DN.decode(dn);
        MemoryBackend memoryBackend = new MemoryBackend();
        memoryBackend = new MemoryBackend();
        memoryBackend.setBackendID("test");
        memoryBackend.setBaseDNs(new DN[] { baseDN });
        memoryBackend.initializeBackend();
        DirectoryServer.registerBackend(memoryBackend);
        
        memoryBackend.clearMemoryBackend();

        return memoryBackend;
    }
	
	protected File getServerRoot()
	{
		try
		{
			File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
			File rootDirectory = new File(tempDirectory, serverRoot);
			
			// Delete the existing directory
			if(rootDirectory.exists())
			{
				FileUtils.deleteDirectory(rootDirectory);
			}
			
			// Create the directory
			rootDirectory.mkdir();
			
	    	return rootDirectory;
		}
		catch(Exception e)
		{
			throw new RuntimeException();
		}
	}
}
