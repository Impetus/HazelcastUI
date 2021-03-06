package com.hazelcast.ui.controller.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.hazelcast.ui.controller.HazelcastRestController;

/**
 * Class to unit test the methods of HazelcastRestController.
 *
 * @author sameena.parveen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring-servlet.xml")
public class HazelcastRestControllerTest {
  private HazelcastInstance instance = null;
  private Map<String, String> mapCustomers = null;
  private static final Logger LOGGER =
			Logger.getLogger(HazelcastRestController.class);
  private int HZ_WAIT_TIME = 10000;

  @Autowired
  private HazelcastRestController hazecastRestController;

  // @Mock
  // private HazelcastRestService hazelcastRestService;

  private static ObjectMapper objectMapper;

  /**
   * Set up method.
   *
   * @throws java.lang.Exception
   *             exception
   */
  @BeforeClass
  public static void setUp() throws Exception {
    System.setProperty("DEPLOY_ENV", "local");
    final Config cfg = new Config();
    cfg.getNetworkConfig().setPublicAddress("localhost:5701");
    final HazelcastRestControllerTest hzTest =
    		new HazelcastRestControllerTest();
    hzTest.initialize(cfg);
  }

  /**
	 * This method checks if HZ instance is up and running before populating map.
	 * @param cfg config
	 * @throws InterruptedException exception
	 */
  public void initialize(final Config cfg) throws InterruptedException {
		instance = HazelcastInstanceFactory.newHazelcastInstance(cfg);
	    while (!instance.getLifecycleService().isRunning()) {
			Thread.sleep(HZ_WAIT_TIME);
		}

	    mapCustomers = instance.getMap("customers");
		mapCustomers.put("1", "Joe");
		mapCustomers.put("2", "Ali");
		mapCustomers.put("3", "Avi");
		objectMapper = new ObjectMapper();
	}

  // @Before
  // public void init() {
  // hazecastRestController.setHazelcastRestService(hazelcastRestService);
  // }

  /**
   * This method tests welcome method of controller class.
   */
  @Test
  public void testWelcome() {
    assertEquals("Welcome to Hazelcast Web UI",
        hazecastRestController.welcome());
  }

  /**
   * This test case is for method which fetches members
   * information from hazelcast.
   * @throws JsonProcessingException .
   */
  @Test
  public void testGetMembersInfo() throws JsonProcessingException {
    assertEquals("[localhost:5701]",
        hazecastRestController.getClusterInfo().replaceAll("\"",
            ""));
  }

  /**
   * This test case is for method which fetches maps information from hazelcast.
   */
  @Test
  public void testGetMapsName() {
    assertEquals("[customers]",
        hazecastRestController.getMapsName().replaceAll("\"", ""));
  }

  /**
   * This test case is for method which fetches a given map's
   * size information from hazelcast.
   * @throws JsonParseException .
   * @throws JsonMappingException .
   * @throws IOException .
   */
  @Test
  public void testGetSize()
      throws JsonParseException, JsonMappingException, IOException {
	  final int value = 3;
    final Map<String, Integer> readValue = objectMapper.readValue(
        hazecastRestController.getSize("customers"),
        new TypeReference<Map<String, Integer>>() {
        });
    assertEquals(value, readValue.get("Size").intValue());
  }

  /**
   * This test case is for method which fetches value for a given key.
   * @throws JsonMappingException json mappin exception
   * @throws JsonParseException json parsing exception
   * @throws IOException IO exception
   * @throws InterruptedException exception
   */
  @Test
  public void testGetValue()
      throws JsonParseException, JsonMappingException,
      IOException, InterruptedException {

	  final String value1 =
        hazecastRestController.getValue("customers", "1", "String");

    final Map<String, String> readValue = objectMapper.readValue(value1,
        new TypeReference<Map<String, String>>() {
        }
    );

    assertEquals("Joe", readValue.get("Value"));

    final Set<String> expectedKeys = new HashSet<>();
    expectedKeys.add("Value");
    expectedKeys.add("Creation_Time");
    expectedKeys.add("Last_Update_Time");
    expectedKeys.add("Last_Access_Time");
    expectedKeys.add("Expiration_Time");

    assertEquals(expectedKeys, readValue.keySet());
  }
  /**
   * This method is to shut down hazelcast.
   * @throws Exception .
   */
  @AfterClass
  public static void cleanup() throws Exception {
	  final HazelcastRestControllerTest hzTest = new HazelcastRestControllerTest();
	  hzTest.shutdownCurrentInstance();
  }

  /**
	* This method shuts down HZ instance spawned by current test case.
	*/
  public void shutdownCurrentInstance() {
	  	if (instance != null) {
	  		instance.shutdown();
	  	}
	}
}
