import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import org.specs2.execute.AsResult

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends PlaySpecification {

  "Twipser" should {
    
    "display created twiips immediately" in new WithBrowser(webDriver = FIREFOX)  { 
   	      val uuid = java.util.UUID.randomUUID.toString

	      browser.goTo("http://localhost:" + testServerPort + "/twiips")
	      browser.$("#author").text("selenium")
   	      browser.$("#message").text(uuid)

   	      browser.$("#create-twiip-form").submit()
   	      browser.await()
	      
	      browser.pageSource must contain(uuid)
    }
    
    "complain about to long messages" in new WithBrowser(webDriver = FIREFOX) {

	      browser.goTo("http://localhost:" + testServerPort + "/twiips")
	      browser.$("#author").text("selenium")
   	      browser.$("#message").text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse pulvinar porta tempus. In a nisi eget leo egestas aliquam nec.")

   	      browser.$("#create-twiip-form").submit()
   	      browser.await()
	      
	      browser.$(".error").getText() must contain("Maximum length is 120")
    }
    
     
    
  }
}
