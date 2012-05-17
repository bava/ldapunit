package org.ldapunit.context;

import org.ldapunit.server.ApacheDSConfigurer;
import org.ldapunit.server.EmbeddedLdapServerType;
import org.ldapunit.server.EmbeddedServerConfigurer;
import org.ldapunit.server.OpenDsConfigurer;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;


public class EmbeddedContextSourceFactory extends AbstractFactoryBean<ContextSource> {
		
	private int port;
	private String rootDn;
	private String base;
	private EmbeddedLdapServerType serverType = EmbeddedLdapServerType.APACHEDS;
	
	private EmbeddedServerConfigurer embeddedServerConfigurer;
	
	@Override
	public Class<?> getObjectType() {
		return ContextSource.class;
	}
	
	@Override
	protected ContextSource createInstance() throws Exception {
		
		if(EmbeddedLdapServerType.APACHEDS.equals(serverType))
		{
			embeddedServerConfigurer = new ApacheDSConfigurer(rootDn, port);
		}
		else if(EmbeddedLdapServerType.OPENDS.equals(serverType))
		{
			embeddedServerConfigurer = new OpenDsConfigurer(rootDn, port);
		}
		else
		{
			// Throw unsupported server exception
			throw new Exception("Unsupported Server " + serverType);
		}
		
		embeddedServerConfigurer.startServer();
		
		LdapContextSource targetContextSource = new LdapContextSource();
		targetContextSource.setUrl("ldap://localhost:" + port);
		targetContextSource.setBase(base);
		targetContextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
		targetContextSource.afterPropertiesSet();
		
		return targetContextSource;
	}
		
	@Override
	protected void destroyInstance(ContextSource instance) throws Exception {
		super.destroyInstance(instance);
		embeddedServerConfigurer.stopServer();
	}
	
	public void setRootDn(String rootDn) {
		this.rootDn = rootDn;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setBase(String base) {
		this.base = base;
	}
	
	public void setServerType(EmbeddedLdapServerType serverType) {
		this.serverType = serverType;
	}
}
