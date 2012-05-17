package org.ldapunit.test;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldapunit.util.LdapUnitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:repositoryContext-test.xml"})
public class TestRepository {

	@Autowired
	LdapTemplate ldapTemplate;
	
	
	@Before
	public void setup() throws Exception
	{
		System.out.println("Inside the setup");
		LdapUnitUtils.loadData(new ClassPathResource("employees.ldif"), "12389");
	}
	
	@After
	public void teardown() throws Exception
	{
		System.out.println("Inside the teardown");
		LdapUnitUtils.clearSubContexts(new DistinguishedName("dc=inflinx,dc=com"), "12389");
	}
	
	@Test
	public void testMethod() {
		System.out.println(getCount(ldapTemplate));
	}
	
	@Test
	public void testMethod2() {
		ldapTemplate.unbind(new DistinguishedName("uid=employee0,ou=employees,dc=inflinx,dc=com"));
		System.out.println(getCount(ldapTemplate));
	}
	
	private int getCount(LdapTemplate ldapTemplate)
	{
		List results = ldapTemplate.search("dc=inflinx,dc=com", "(objectClass=inetorgperson)", new ContextMapper() {
			@Override
			public Object mapFromContext(Object ctx) {
				return ((DirContextAdapter)ctx).getDn();
			}
		});
		
		return results.size();
	}		
}
