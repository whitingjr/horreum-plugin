package jenkins.plugins.horreum_upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.junit.Ignore;
import org.junit.Test;

import jenkins.plugins.horreum_upload.api.dto.HorreumTest;

public class HorreumClientTest extends HorreumPluginTestBase {

	@Test
	@Ignore
	public void horreumApiTestGetID(){
		HorreumTest test = horreumTestProxy.get(10, null);
		assertNotNull(test);

		assertEquals("Dummy", test.getName());
	}

	@Test
	@Ignore
	public void horreumApiNewTest(){
		HorreumTest newTest = new HorreumTest();
		newTest.setName("Dev Ops Test");
		newTest.setOwner("dev-team");

		ClientResponse response = horreumTestProxy.add(newTest);
		assertNotNull(response);

		assertEquals(200, response.getStatus());
	}
}
