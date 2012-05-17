package org.ldapunit.test;

import org.ldapunit.server.ApacheDSConfigurer;
import org.ldapunit.server.EmbeddedServerConfigurer;
import org.ldapunit.server.OpenDsConfigurer;

public class TestNonSpring 
{
	public static void main(String[] args) 
	{
		/*
		EmbeddedServerConfigurer server = new ApacheDSConfigurer("dc=inflinx,dc=com", 11339);
		server.startServer();
		server.stopServer();		
		*/
		
		EmbeddedServerConfigurer server = new OpenDsConfigurer("dc=inflinx,dc=com", 12339);
		server.startServer();
		server.stopServer();
	}
}
