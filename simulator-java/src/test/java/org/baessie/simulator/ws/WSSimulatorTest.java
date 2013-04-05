package org.baessie.simulator.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.baessie.simulator.SimulatorException;
import org.baessie.simulator.SimulatorRequest;
import org.baessie.simulator.SimulatorResponse;
import org.baessie.simulator.util.SimulatorUtils;
import org.baessie.simulator.ws.WSSimulator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class WSSimulatorTest {

   private WSSimulator simulator;

   @Before
   public void setup() {
      simulator = new WSSimulator();
   }

   @Test
   public void setupTestMinimal() {
      // SETUP
      final SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "MINIMAL");
      request.addParameter(WSSimulator.IN_DATA, "<request/>");
      request.addParameter(WSSimulator.OUT_DATA, "<response/>");

      final String output = simulator.setupTest(request);

      assertEquals("Added test", "MINIMAL", output);
   }

   @Test
   public void executeTestScanBackReferences() throws Exception {
      final String testRequest = "<request><in>1</in><in>*(TWO)*</in><in>*(THREE)*</in><in>4</in></request>";
      final String testResponse = "<response><out>A</out><out>*(TWO)*</out><group><out>*(THREE)*</out><out>*(TWO)*</out><out> 1 *(TWO)* 3 </out></group></response>";
      final String realRequest = "<request><in>1</in><in>2</in><in>3</in><in>4</in></request>";
      final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><response><out>A</out><out>2</out><group><out>3</out><out>2</out><out> 1 2 3 </out></group></response>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, new String[] { "SCAN BACK REFERENCES" });
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);

      String output = simulator.setupTest(request);

      assertEquals("Added test", "SCAN BACK REFERENCES", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      final SimulatorResponse response = simulator.executeTest(request);
      output = createResponseString(response);

      assertEquals("Response with replaced back references", expectedResponse, output);

      final Integer result = simulator.verifyTest("SCAN BACK REFERENCES");

      assertEquals("Test called once", Integer.valueOf(1), result);
   }

   @Test
   public void executeTestWithBackReferences() throws Exception {
      final String testRequest = "<x:request xmlns:x=\"http://x\"><in>1</in><in>*(TWO)*</in><in>*(THREE)*</in><in>4</in></x:request>";
      final String testResponse = "<y:response xmlns:y=\"http://x\"><out>A</out><out>*(TWO)*</out><group><out>*(THREE)*</out><out>*(TWO)*</out><out> 1 *(TWO)* 3 </out></group></y:response>";
      final String realRequest = "<z:request xmlns:z=\"http://x\"><in>1</in><in>2</in><in>3</in><in>4</in></z:request>";
      final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><y:response xmlns:y=\"http://x\"><out>A</out><out>2</out><group><out>3</out><out>2</out><out> 1 2 3 </out></group></y:response>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "WITH BACK REFERENCES");
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);
      request.addParameter(WSSimulator.IN_BACK_REFERENCES, "/x:request[1]/in[2]/text()[1]", "/x:request[1]/in[3]/text()[1]");
      request.addParameter(WSSimulator.OUT_BACK_REFERENCES, "/y:response[1]/out[2]/text()[1]", "/y:response[1]/group[1]/out[1]/text()[1]",
            "/y:response[1]/group[1]/out[2]/text()[1]", "/y:response[1]/group[1]/out[3]/text()[1]");
      request.addParameter(WSSimulator.SCAN_FOR_BACK_REFERENCES, "false");

      String output = simulator.setupTest(request);

      assertEquals("Added test", "WITH BACK REFERENCES", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      final SimulatorResponse response = simulator.executeTest(request);
      output = createResponseString(response);

      assertEquals("Response with replaced back references", expectedResponse, output);

      final Integer result = simulator.verifyTest("WITH BACK REFERENCES");

      assertEquals("Test called once", Integer.valueOf(1), result);

   }

   @Test
   public void executeTestHeadersAdded() throws Exception {
      final String testRequest = "<request><in>1</in><in>2</in><in>3</in><in>4</in></request>";
      final String testResponse = "<response><out>A</out><out>2</out><group><out>3</out><out>2</out><out> 1 2 3 </out></group></response>";
      final String realRequest = "<request><in>1</in><in>2</in><in>3</in><in>4</in></request>";
      final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><response><out>A</out><out>2</out><group><out>3</out><out>2</out><out> 1 2 3 </out></group></response>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "WITH HEADERS");
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);
      request.addParameter(WSSimulator.RESPONSE_HEADERS, new String[] { "gnurf=apa" });

      String output = simulator.setupTest(request);

      assertEquals("Test setup", "WITH HEADERS", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      final SimulatorResponse response = simulator.executeTest(request);
      output = createResponseString(response);

      assertEquals("Response with replaced back references", expectedResponse, output);

      assertEquals("Should have the 'appropriate' header", "apa", response.getHeaders().get("gnurf"));

      final Integer result = simulator.verifyTest("WITH HEADERS");

      assertEquals("Test called once", Integer.valueOf(1), result);

   }

   @Test
   public void executeTestDelay() throws Exception {
      final String testRequest = "<request><in>1</in><in>2</in><in>3</in><in>4</in></request>";
      final String testResponse = "<response><out>A</out><out>2</out><group><out>3</out><out>2</out><out> 1 2 3 </out></group></response>";
      final String realRequest = "<request><in>1</in><in>2</in><in>3</in><in>4</in></request>";
      final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><response><out>A</out><out>2</out><group><out>3</out><out>2</out><out> 1 2 3 </out></group></response>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "WITH HEADERS");
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);
      request.addParameter(WSSimulator.DELAY, "150");

      String output = simulator.setupTest(request);

      assertEquals("Test setup", "WITH HEADERS", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      final DateTime beforeCall = new DateTime();
      final SimulatorResponse response = simulator.executeTest(request);
      final DateTime afterCall = new DateTime();

      final long msBetween = afterCall.getMillis() - beforeCall.getMillis();
      assertTrue("should be more than 150 ms between before and after", msBetween > 150);

      output = createResponseString(response);

      assertEquals("Response with replaced back references", expectedResponse, output);

      final Integer result = simulator.verifyTest("WITH HEADERS");

      assertEquals("Test called once", Integer.valueOf(1), result);

   }

   @Test(expected = SimulatorException.class)
   public void executeTestFailToMatch() throws Exception {
      final String testRequest = "<request><in>1</in><in>2</in><in>3</in><in>4</in></request>";
      final String testResponse = "<response><out>A</out><out>2</out><group><out>3</out><out>2</out><out> 1 2 3 </out></group></response>";
      final String realRequest = "<request><in>APPLE</in><in>2</in><in>3</in><in>4</in></request>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "WITH HEADERS");
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);
      request.addParameter(WSSimulator.RESPONSE_HEADERS, "gnurf=apa");

      final String output = simulator.setupTest(request);

      assertEquals("Setup should match", "WITH HEADERS", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      simulator.executeTest(request);

   }

   @Test
   public void executeTestAllowChildNodesInDifferentOrder() throws Exception {
      final String testRequest = "<request><in>1</in><in>2</in><in>3</in><in>4</in></request>";
      final String testResponse = "<response><out>A</out><out>2</out><group><out>3</out><out>2</out><out> 1 2 3 </out></group></response>";
      final String realRequest = "<request><in>3</in><in>4</in><in>1</in><in>2</in></request>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "WITH HEADERS");
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);
      request.addParameter(WSSimulator.RESPONSE_HEADERS, "gnurf=apa");

      final String output = simulator.setupTest(request);

      assertEquals("Setup should match", "WITH HEADERS", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      simulator.executeTest(request);

   }

   @Test
   public void executeTestWithAttributeBackReferences() throws Exception {
      final String testRequest = "<request test1=\"*(TEST-1)*\"><in test2=\"*(TEST-2)*\">*(TEST-3)*<x text4=\"*(TEST-4)*\">*(TEST-5)*</x></in></request>";
      final String testResponse = "<response attr=\" *(TEST-1)* \"> *(TEST-2)* *(TEST-3)* *(TEST-4)* *(TEST-5)* </response>";
      final String realRequest = "<request test1=\"A\"><in test2=\"B\">C<x text4=\"D\">E</x></in></request>";
      final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><response attr=\" A \"> B C D E </response>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "WITH ATTRIBUTE BACK REFERENCES");
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);

      String output = simulator.setupTest(request);

      assertEquals("Test called once", "WITH ATTRIBUTE BACK REFERENCES", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      final SimulatorResponse response = simulator.executeTest(request);
      output = createResponseString(response);

      assertEquals("Response with replaced back references", expectedResponse, output);

      final Integer result = simulator.verifyTest("WITH ATTRIBUTE BACK REFERENCES");

      assertEquals("Test called once", Integer.valueOf(1), result);
   }

   @Test
   public void executeTestGnurfApp() throws Exception {
      final String realRequest = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"><soap:Header><Action xmlns=\"http://www.w3.org/2005/08/addressing\">http://www.gnurfapp.se/NWL/IServiceProvider/IServiceProvider/SearchAddress</Action><MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">urn:uuid:ae3fb423-c934-4683-b9db-5b889c7b10b9</MessageID><To xmlns=\"http://www.w3.org/2005/08/addressing\">http://localhost:7001/basesim/</To><ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\"><Address>http://www.w3.org/2005/08/addressing/anonymous</Address></ReplyTo></soap:Header><soap:Body><ns3:SearchAddress xmlns:ns1=\"http://schemas.microsoft.com/2003/10/Serialization/\" xmlns:ns2=\"http://www.gnurfapp.se/NDL/AddressProvider/\" xmlns:ns3=\"http://www.gnurfapp.se/NWL/IServiceProvider/\" xmlns:ns4=\"http://www.gnurfapp.se/NDL/Service/\" xmlns:ns5=\"http://www.gnurfapp.se/NDL/ServiceProvider/\" xmlns:ns6=\"http://www.gnurfapp.se/NDL/ServiceProviderUser/\" xmlns:ns7=\"http://www.gnurfapp.se/NWL/Authentication/UserAuthentication/\" xmlns:ns8=\"http://www.gnurfapp.se/NDL/VlanDefinition/\" xmlns:ns9=\"http://schemas.datacontract.org/2004/07/GnurfApp.Developers.Library.Data\" xmlns:ns10=\"http://www.gnurfapp.se/NDL/AlertGroup/\" xmlns:ns11=\"http://www.gnurfapp.se/NDL/Equipment/\" xmlns:ns12=\"http://www.gnurfapp.se/NDL/Company/\" xmlns:ns13=\"http://www.gnurfapp.se/NDL/EquipmentPort/\" xmlns:ns14=\"http://www.gnurfapp.se/NDL/Address/\" xmlns:ns15=\"http://www.gnurfapp.se/NDL/Customer/\" xmlns:ns16=\"http://www.gnurfapp.se/NDL/Debit/\" xmlns:ns17=\"http://www.gnurfapp.se/NDL/Item/\" xmlns:ns18=\"http://www.gnurfapp.se/NDL/Subscription/\" xmlns:ns19=\"http://www.gnurfapp.se/NDL/CustomerSP/\" xmlns:ns20=\"http://www.gnurfapp.se/NDL/NetAddress/\" xmlns:ns21=\"http://www.gnurfapp.se/NDL/MonitoringEvent/\" xmlns:ns22=\"http://www.gnurfapp.se/NDL/SiteRegion/\" xmlns:ns23=\"http://www.gnurfapp.se/NDL/SiteCity/\" xmlns:ns24=\"http://www.gnurfapp.se/NDL/SiteArea/\" xmlns:ns25=\"http://www.gnurfapp.se/NDL/Site/\" xmlns:ns26=\"http://www.gnurfapp.se/NDL/MonitoringGraph/\" xmlns:ns27=\"http://www.gnurfapp.se/NDL/MonitoringService/\" xmlns:ns28=\"http://www.gnurfapp.se/NDL/PhysicalConnection/\" xmlns:ns29=\"http://www.gnurfapp.se/NDL/SiteCountry/\" xmlns:ns30=\"http://www.gnurfapp.se/NDL/CustomerServiceUser/\" xmlns:ns31=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\" xmlns:ns32=\"http://www.gnurfapp.se/NDL/TechnicalUser/\" xmlns:ns33=\"http://www.gnurfapp.se/NDL/CustomerAddress/\" xmlns:ns34=\"http://www.gnurfapp.se/NDL/Case/\" xmlns:ns35=\"http://www.gnurfapp.se/NDL/CasePost/\" xmlns:ns36=\"http://www.gnurfapp.se/NDL/SubscriptionJob/\" xmlns:ns37=\"http://schemas.datacontract.org/2004/07/GnurfApp.Webservice.Library.ErrorHandling\" xmlns:ns38=\"http://www.gnurfapp.se/NDL/ChangeSubscription/\" xmlns:ns39=\"http://www.gnurfapp.se/NDL/Relocation/\" xmlns:ns40=\"http://www.gnurfapp.se/NDL/Success/\" xmlns:ns41=\"http://www.gnurfapp.se/NDL/Transfer/\" xmlns:ns42=\"http://schemas.datacontract.org/2004/07/GnurfApp.Objects.Subscription\" xmlns:ns43=\"http://schemas.datacontract.org/2004/07/GnurfApp.Developers.Library.Base\" xmlns:ns44=\"http://www.gnurfapp.se/NDL/Base/\"><ns3:address><ns14:AddressCode1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:AddressCode2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:AddressCode3 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:AddressCode4 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:AddressCode5 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:AddressIdentity>123321456</ns14:AddressIdentity><ns14:ApartmentAddress xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:ApartmentNumber xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Area xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:City xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Country xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:County xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Customer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Entrance xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:EquipmentPort xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Floor xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Municipality xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Owner xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:PostalCode xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Search xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:ServiceProviderCustomer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Street xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:StreetNumber xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:Subscription xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></ns3:address><ns3:io><ns14:Customer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:EquipmentPort xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns14:ServiceProviderCustomer><ns19:Address xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns19:Customer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns19:ServiceProvider xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></ns14:ServiceProviderCustomer><ns14:Subscription xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></ns3:io><ns3:auth><ns7:IpAddress xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns7:Password>password</ns7:Password><ns7:UserName>simulator</ns7:UserName></ns3:auth></ns3:SearchAddress></soap:Body></soap:Envelope>";
      final String testRequest = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"><soap:Header><Action xmlns=\"http://www.w3.org/2005/08/addressing\">http://www.gnurfapp.se/NWL/IServiceProvider/IServiceProvider/SearchAddress</Action><MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">*(id)*</MessageID><To xmlns=\"http://www.w3.org/2005/08/addressing\">*</To><ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\"><Address>http://www.w3.org/2005/08/addressing/anonymous</Address></ReplyTo></soap:Header><soap:Body><ns3:SearchAddress xmlns:ns1=\"http://schemas.microsoft.com/2003/10/Serialization/\" xmlns:ns2=\"http://www.gnurfapp.se/NDL/AddressProvider/\" xmlns:ns3=\"http://www.gnurfapp.se/NWL/IServiceProvider/\" xmlns:ns4=\"http://www.gnurfapp.se/NDL/Service/\" xmlns:ns5=\"http://www.gnurfapp.se/NDL/ServiceProvider/\" xmlns:ns6=\"http://www.gnurfapp.se/NDL/ServiceProviderUser/\" xmlns:ns7=\"http://www.gnurfapp.se/NWL/Authentication/UserAuthentication/\" xmlns:ns8=\"http://www.gnurfapp.se/NDL/AlertGroup/\" xmlns:ns9=\"http://www.gnurfapp.se/NDL/Equipment/\" xmlns:ns10=\"http://www.gnurfapp.se/NDL/Company/\" xmlns:ns11=\"http://www.gnurfapp.se/NDL/EquipmentPort/\" xmlns:ns12=\"http://www.gnurfapp.se/NDL/Address/\" xmlns:ns13=\"http://www.gnurfapp.se/NDL/Customer/\" xmlns:ns14=\"http://www.gnurfapp.se/NDL/Debit/\" xmlns:ns15=\"http://www.gnurfapp.se/NDL/Item/\" xmlns:ns16=\"http://www.gnurfapp.se/NDL/VlanDefinition/\" xmlns:ns17=\"http://schemas.datacontract.org/2004/07/GnurfApp.Developers.Library.Data\" xmlns:ns18=\"http://www.gnurfapp.se/NDL/Subscription/\" xmlns:ns19=\"http://www.gnurfapp.se/NDL/CustomerSP/\" xmlns:ns20=\"http://www.gnurfapp.se/NDL/NetAddress/\" xmlns:ns21=\"http://www.gnurfapp.se/NDL/MonitoringEvent/\" xmlns:ns22=\"http://www.gnurfapp.se/NDL/SiteRegion/\" xmlns:ns23=\"http://www.gnurfapp.se/NDL/SiteCity/\" xmlns:ns24=\"http://www.gnurfapp.se/NDL/SiteArea/\" xmlns:ns25=\"http://www.gnurfapp.se/NDL/Site/\" xmlns:ns26=\"http://www.gnurfapp.se/NDL/MonitoringGraph/\" xmlns:ns27=\"http://www.gnurfapp.se/NDL/MonitoringService/\" xmlns:ns28=\"http://www.gnurfapp.se/NDL/PhysicalConnection/\" xmlns:ns29=\"http://www.gnurfapp.se/NDL/SiteCountry/\" xmlns:ns30=\"http://www.gnurfapp.se/NDL/CustomerAddress/\" xmlns:ns31=\"http://www.gnurfapp.se/NDL/CasePost/\" xmlns:ns32=\"http://www.gnurfapp.se/NDL/Case/\" xmlns:ns33=\"http://www.gnurfapp.se/NDL/CustomerServiceUser/\" xmlns:ns34=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\" xmlns:ns35=\"http://www.gnurfapp.se/NDL/TechnicalUser/\" xmlns:ns36=\"http://www.gnurfapp.se/NDL/SubscriptionJob/\" xmlns:ns37=\"http://schemas.datacontract.org/2004/07/GnurfApp.Webservice.Library.ErrorHandling\" xmlns:ns38=\"http://www.gnurfapp.se/NDL/ChangeSubscription/\" xmlns:ns39=\"http://www.gnurfapp.se/NDL/Relocation/\" xmlns:ns40=\"http://www.gnurfapp.se/NDL/Success/\" xmlns:ns41=\"http://www.gnurfapp.se/NDL/Transfer/\" xmlns:ns42=\"http://www.gnurfapp.se/NDL/Base/\" xmlns:ns43=\"http://schemas.datacontract.org/2004/07/GnurfApp.Developers.Library.Base\" xmlns:ns44=\"http://schemas.datacontract.org/2004/07/GnurfApp.Objects.Subscription\"><ns3:address><ns12:AddressCode1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:AddressCode2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:AddressCode3 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:AddressCode4 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:AddressCode5 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:AddressIdentity>123321456</ns12:AddressIdentity><ns12:ApartmentAddress xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:ApartmentNumber xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Area xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:City xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Country xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:County xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Customer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Entrance xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:EquipmentPort xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Floor xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Municipality xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Owner xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:PostalCode xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Search xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:ServiceProviderCustomer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Street xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:StreetNumber xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:Subscription xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></ns3:address><ns3:io><ns12:Customer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:EquipmentPort xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns12:ServiceProviderCustomer><ns19:Address xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns19:Customer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns19:ServiceProvider xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></ns12:ServiceProviderCustomer><ns12:Subscription xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></ns3:io><ns3:auth><ns7:IpAddress xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><ns7:Password>*</ns7:Password><ns7:UserName>*</ns7:UserName></ns3:auth></ns3:SearchAddress></soap:Body></soap:Envelope>";
      final String testResponse = "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\"><s:Header><a:Action s:mustUnderstand=\"1\">http://www.gnurfapp.se/NWL/IServiceProvider/IServiceProvider/SearchAddressResponse</a:Action><a:RelatesTo>*(id)*</a:RelatesTo></s:Header><s:Body><SearchAddressResponse xmlns=\"http://www.gnurfapp.se/NWL/IServiceProvider/\"><SearchAddressResult xmlns:b=\"http://www.gnurfapp.se/NDL/Address/\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><b:Address z:Id=\"i1\" xmlns:z=\"http://schemas.microsoft.com/2003/10/Serialization/\"><b:AddressCode1>BREDBANDSSWITCH_JA</b:AddressCode1><b:AddressCode2/><b:AddressCode3>KO36634</b:AddressCode3><b:AddressCode4/><b:AddressCode5/><b:AddressIdentity>123321456</b:AddressIdentity><b:ApartmentAddress>214-003-02-045</b:ApartmentAddress><b:ApartmentNumber>214-003-02-045</b:ApartmentNumber><b:Area>BENGTSFORS</b:Area><b:City>BENGTSFORS</b:City><b:CompanyID>1182</b:CompanyID><b:Country>SVERIGE</b:Country><b:County>VÄSTRA GÖTALAND</b:County><b:Customer i:nil=\"true\" xmlns:c=\"http://www.gnurfapp.se/NDL/Customer/\"/><b:Entrance/><b:EquipmentPort i:nil=\"true\" xmlns:c=\"http://www.gnurfapp.se/NDL/EquipmentPort/\"/><b:Floor>3</b:Floor><b:ID>327691</b:ID><b:Municipality/><b:Owner>Bengtsfors FTG AB 2.1.1</b:Owner><b:PostalCode>66600</b:PostalCode><b:Search z:Id=\"i2\"><b:GroupBy>None</b:GroupBy><b:Limit>0</b:Limit><b:OrderBy>None</b:OrderBy><b:OrderByDirection>ASC</b:OrderByDirection></b:Search><b:ServiceProviderCustomer xmlns:c=\"http://www.gnurfapp.se/NDL/CustomerSP/\"><c:CustomerSP z:Id=\"i3\"><c:Address i:nil=\"true\"/><c:AgreementNo/><c:ApartmentNumber>214-003-02-045</c:ApartmentNumber><c:City>BENGTSFORS</c:City><c:CompanyID>1182</c:CompanyID><c:ContactCity>BENGTSFORS</c:ContactCity><c:ContactCountry>SVERIGE</c:ContactCountry><c:ContactEmail/><c:ContactFirstName>Birgitta</c:ContactFirstName><c:ContactLastName>Kemppinen</c:ContactLastName><c:ContactPhone/><c:ContactPhoneCell>0046732005688</c:ContactPhoneCell><c:ContactPhoneWork/><c:ContactPostalCode>66600</c:ContactPostalCode><c:ContactStreet>STORGATAN 41</c:ContactStreet><c:Corporate>false</c:Corporate><c:Country>SVERIGE</c:Country><c:Customer i:nil=\"true\" xmlns:d=\"http://www.gnurfapp.se/NDL/Customer/\"/><c:CustomerID>123456789</c:CustomerID><c:CustomerNumber>99999999</c:CustomerNumber><c:DateCreated>2010-09-29T00:00:00</c:DateCreated><c:DateToBeDeleted>0001-01-01T00:00:00</c:DateToBeDeleted><c:DateUpdated>0001-01-01T00:00:00</c:DateUpdated><c:ExcludeInMessageService>false</c:ExcludeInMessageService><c:FaxNumber/><c:FirstName>Kemppinen</c:FirstName><c:LastName>Birgitta</c:LastName><c:PostalCode>66600</c:PostalCode><c:ProviderID>1</c:ProviderID><c:Reference>Bengtsfors FTG AB 2.1.1</c:Reference><c:SSN>19510316-6343</c:SSN><c:ServiceProvider i:nil=\"true\" xmlns:d=\"http://www.gnurfapp.se/NDL/ServiceProvider/\"/><c:Street>STORGATAN 41 3 tr</c:Street></c:CustomerSP></b:ServiceProviderCustomer><b:Street>STORGATAN</b:Street><b:StreetNumber>41</b:StreetNumber><b:Subscription i:nil=\"true\" xmlns:c=\"http://www.gnurfapp.se/NDL/Subscription/\"/></b:Address></SearchAddressResult></SearchAddressResponse></s:Body></s:Envelope>";
      final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\"><s:Header><a:Action s:mustUnderstand=\"1\">http://www.gnurfapp.se/NWL/IServiceProvider/IServiceProvider/SearchAddressResponse</a:Action><a:RelatesTo>urn:uuid:ae3fb423-c934-4683-b9db-5b889c7b10b9</a:RelatesTo></s:Header><s:Body><SearchAddressResponse xmlns=\"http://www.gnurfapp.se/NWL/IServiceProvider/\"><SearchAddressResult xmlns:b=\"http://www.gnurfapp.se/NDL/Address/\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><b:Address xmlns:z=\"http://schemas.microsoft.com/2003/10/Serialization/\" z:Id=\"i1\"><b:AddressCode1>BREDBANDSSWITCH_JA</b:AddressCode1><b:AddressCode2/><b:AddressCode3>KO36634</b:AddressCode3><b:AddressCode4/><b:AddressCode5/><b:AddressIdentity>123321456</b:AddressIdentity><b:ApartmentAddress>214-003-02-045</b:ApartmentAddress><b:ApartmentNumber>214-003-02-045</b:ApartmentNumber><b:Area>BENGTSFORS</b:Area><b:City>BENGTSFORS</b:City><b:CompanyID>1182</b:CompanyID><b:Country>SVERIGE</b:Country><b:County>VÄSTRA GÖTALAND</b:County><b:Customer xmlns:c=\"http://www.gnurfapp.se/NDL/Customer/\" i:nil=\"true\"/><b:Entrance/><b:EquipmentPort xmlns:c=\"http://www.gnurfapp.se/NDL/EquipmentPort/\" i:nil=\"true\"/><b:Floor>3</b:Floor><b:ID>327691</b:ID><b:Municipality/><b:Owner>Bengtsfors FTG AB 2.1.1</b:Owner><b:PostalCode>66600</b:PostalCode><b:Search z:Id=\"i2\"><b:GroupBy>None</b:GroupBy><b:Limit>0</b:Limit><b:OrderBy>None</b:OrderBy><b:OrderByDirection>ASC</b:OrderByDirection></b:Search><b:ServiceProviderCustomer xmlns:c=\"http://www.gnurfapp.se/NDL/CustomerSP/\"><c:CustomerSP z:Id=\"i3\"><c:Address i:nil=\"true\"/><c:AgreementNo/><c:ApartmentNumber>214-003-02-045</c:ApartmentNumber><c:City>BENGTSFORS</c:City><c:CompanyID>1182</c:CompanyID><c:ContactCity>BENGTSFORS</c:ContactCity><c:ContactCountry>SVERIGE</c:ContactCountry><c:ContactEmail/><c:ContactFirstName>Birgitta</c:ContactFirstName><c:ContactLastName>Kemppinen</c:ContactLastName><c:ContactPhone/><c:ContactPhoneCell>0046732005688</c:ContactPhoneCell><c:ContactPhoneWork/><c:ContactPostalCode>66600</c:ContactPostalCode><c:ContactStreet>STORGATAN 41</c:ContactStreet><c:Corporate>false</c:Corporate><c:Country>SVERIGE</c:Country><c:Customer xmlns:d=\"http://www.gnurfapp.se/NDL/Customer/\" i:nil=\"true\"/><c:CustomerID>123456789</c:CustomerID><c:CustomerNumber>99999999</c:CustomerNumber><c:DateCreated>2010-09-29T00:00:00</c:DateCreated><c:DateToBeDeleted>0001-01-01T00:00:00</c:DateToBeDeleted><c:DateUpdated>0001-01-01T00:00:00</c:DateUpdated><c:ExcludeInMessageService>false</c:ExcludeInMessageService><c:FaxNumber/><c:FirstName>Kemppinen</c:FirstName><c:LastName>Birgitta</c:LastName><c:PostalCode>66600</c:PostalCode><c:ProviderID>1</c:ProviderID><c:Reference>Bengtsfors FTG AB 2.1.1</c:Reference><c:SSN>19510316-6343</c:SSN><c:ServiceProvider xmlns:d=\"http://www.gnurfapp.se/NDL/ServiceProvider/\" i:nil=\"true\"/><c:Street>STORGATAN 41 3 tr</c:Street></c:CustomerSP></b:ServiceProviderCustomer><b:Street>STORGATAN</b:Street><b:StreetNumber>41</b:StreetNumber><b:Subscription xmlns:c=\"http://www.gnurfapp.se/NDL/Subscription/\" i:nil=\"true\"/></b:Address></SearchAddressResult></SearchAddressResponse></s:Body></s:Envelope>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "GNURFAPP");
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);

      String output = simulator.setupTest(request);

      assertEquals("Added test", "GNURFAPP", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      final SimulatorResponse response = simulator.executeTest(request);
      output = createResponseString(response);

      assertEquals("Response with replaced back references", expectedResponse, output);

      final Integer result = simulator.verifyTest("GNURFAPP");

      assertEquals("Test called once", Integer.valueOf(1), result);
   }

   @Test
   public void executeTestWildcard() throws Exception {
      final String testRequest = "<request><in>Hej *</in><in>*</in></request>";
      final String testResponse = "<response/>";
      final String realRequest = "<request><in>Hej hopp</in><in>!</in></request>";
      final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><response/>";

      SimulatorRequest request = new SimulatorRequest();
      request.addParameter(SimulatorUtils.TEST_ID, "WILDCARD");
      request.addParameter(WSSimulator.IN_DATA, testRequest);
      request.addParameter(WSSimulator.OUT_DATA, testResponse);

      String output = simulator.setupTest(request);

      assertEquals("Added test", "WILDCARD", output);

      request = new SimulatorRequest();
      final String charset = "UTF-8";
      request.setInputStream(createInputStream(realRequest, charset));
      request.setCharset(charset);

      final SimulatorResponse response = simulator.executeTest(request);
      output = createResponseString(response);

      assertEquals("Response with replaced back references", expectedResponse, output);

      final Integer result = simulator.verifyTest("WILDCARD");

      assertEquals("Test called once", Integer.valueOf(1), result);

   }

   private InputStream createInputStream(final String str, final String charset) {
      InputStream inputStream = null;
      try {
         inputStream = new BufferedInputStream(new ByteArrayInputStream(str.getBytes(charset)));
      } catch (final Exception e) {
         inputStream = new BufferedInputStream(new ByteArrayInputStream("".getBytes()));
      }
      return inputStream;
   }

   private String createResponseString(final SimulatorResponse response) throws Exception {
      ByteArrayOutputStream out = null;
      final Document document = response.getDocument();
      try {
         out = new ByteArrayOutputStream();
         final DOMSource source = new DOMSource(document);
         final StreamResult result = new StreamResult(out);
         SimulatorUtils.newTransformer().transform(source, result);
         out.flush();
         String charset = document.getXmlEncoding();
         if (charset == null) {
            charset = "UTF-8";
         }
         return out.toString(charset);
      } finally {
         if (out != null) {
            try {
               out.close();
            } catch (final IOException e) {
            }
         }
      }
   }
}
