package jenkins.plugins.horreum;

import io.hyperfoil.tools.horreum.api.data.Test;
import jenkins.plugins.horreum.junit.HorreumTestClientExtension;
import jenkins.plugins.horreum.junit.HorreumTestExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import static jenkins.plugins.horreum.junit.HorreumTestClientExtension.getHorreumClient;

@ExtendWith(HorreumTestClientExtension.class)
public class HorreumPluginTestBase {

	Test createTest(String name, String owner) {
		Test test = new Test();
		test.name = name;
		test.owner = owner;
		return getHorreumClient().testService.add(test);
	}

	@BeforeAll
	public static void before() throws Exception {
		HorreumTestExtension.beforeAll();
	}
}
