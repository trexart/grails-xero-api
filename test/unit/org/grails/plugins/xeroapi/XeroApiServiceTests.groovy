package grails.plugins.xeroapi



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(XeroApiService)
class XeroApiServiceTests {

    void testSomething() {
        def xeroService = new XeroApiService()
        String ret = xeroService.serviceMethod()
        
        assert ret == "hello"
        
    }
}
